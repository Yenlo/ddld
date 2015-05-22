import sys
import re
import logging

from lxml import etree
import json

from dateutil.parser import parse

# logging.setLevel(logging.DEBUG)

namespaces = {
    'oai': 'http://www.openarchives.org/OAI/2.0/',
    'didl': 'urn:mpeg:mpeg21:2002:02-DIDL-NS',
    'mods': 'http://www.loc.gov/mods/v3',
    'dai': 'info:eu-repo/dai',
    'dii': 'urn:mpeg:mpeg21:2002:01-DII-NS'
    }


class NameException(BaseException):
    pass


def xpath(elt, path):
    return elt.xpath(path, namespaces=namespaces)


def xpath_str(elt, path):
    return unicode(elt.xpath("normalize-space(%s)" % path, namespaces=namespaces))


def get_name(family, given, display_name):
    if family and given:
        # "Leeuwen,  van" -> "van Leeuwen"
        match = re.match(r'^([^,]+),\s+(.*)$', family)

        family = " ".join([match.group(2), match.group(1)]) if match else family
        name = " ".join([given, family])

        if name != display_name:
            logging.debug('name != display_name: "%s" "%s"', name, display_name)
    elif display_name:
        name = display_name
    else:
        raise NameException

    if re.search(r"\bet al\b", name, flags=re.IGNORECASE):
        logging.debug("skipping 'et al' author: ('%s', '%s', '%s')", family, given, display_name)
        # An "et al" author, really?
        raise NameException

    return name


def get_topics(text):
    res = re.split(r';\s*', text) if text else []
    res = [e.strip() for e in res if not re.match(r'^(?:-{1,}|unknown|\.{2,}|-)$', e, flags=re.IGNORECASE)]
    return res


fname = sys.argv[1]
doc = etree.parse(fname)

for record in xpath(doc, '//oai:record'):
    if xpath(record, 'oai:header[@status="deleted"]'):
        continue

    record_id = unicode(xpath(record, 'string(oai:header/oai:identifier)'))
    # xml bullshit
    persistent_id = xpath(record, 'string(oai:metadata/didl:DIDL/didl:Item/didl:Descriptor/didl:Statement[@mimeType="application/xml"]/dii:Identifier)')

    try:
        mods = xpath(record, './/mods:mods')[0]
    except IndexError:
        logging.exception("no mods element in record(%s) in file '%s'", record_id, fname)
        continue

    authors = []
    for name in xpath(mods, 'mods:name[@type="personal"]'):
        family = xpath_str(name, 'mods:namePart[@type="family"]')
        given = xpath_str(name, 'mods:namePart[@type="given"]')
        display_name = xpath_str(name, 'mods:displayForm')

        try:
            name = get_name(family, given, display_name)
            logging.warn('found name "%s"', name)
            authors.append(name)
        except NameException:
            continue

    abstract = xpath(mods, 'normalize-space(mods:abstract)')
    title = xpath(mods, 'normalize-space(mods:titleInfo/mods:title)')
    date_str = xpath(mods, 'normalize-space(mods:originInfo/mods:dateIssued)')
    date = parse(date_str).isoformat() if date_str else ""
    logging.debug('date str: "%s", date: "%s"', date_str, date)

    topics = []
    for topic_elt in xpath(mods, 'mods:subject/mods:topic'):
        topic_str = xpath_str(topic_elt, '.')
        topic_lst = get_topics(topic_str)
        for topic in topic_lst:
            logging.warn('found topic: "%s"', topic.lower())
        topics.extend(topic_lst)
    
    # if not abstract:
    #     continue

    print json.dumps({'persistent_id': persistent_id,
                      'authors': authors,
                      'abstract': abstract,
                      'title': title,
                      'date': date,
                      'topics': topics
                      })
