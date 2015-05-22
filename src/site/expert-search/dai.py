import sys
from lxml import etree

namespaces = {
    'mods': 'http://www.loc.gov/mods/v3',
    'dai': 'info:eu-repo/dai'
    }


def xpath(elt, path):
    return elt.xpath(path, namespaces=namespaces)

fname = sys.argv[1], sys.argv[2]
dai
doc = etree.parse(fname)

for dai_identifier in xpath(doc, '//dai:identifier'):
    try:
        dai = xpath(dai_identifier, 'normalize-space()')
        author_id = dai_identifier.attrib['IDref']
        dai_authority = dai_identifier.attrib['authority']
    except KeyError:
        print >> sys.stderr, "dai has no ID: ", etree.tostring(dai)
        continue

    print "\t".join(unicode(e).encode("utf-8") for e in [dai, dai_authority, author_id])
