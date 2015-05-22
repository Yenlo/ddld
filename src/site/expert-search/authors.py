import sys
from lxml import etree

namespaces = {
    'mods': 'http://www.loc.gov/mods/v3',
    'dai': 'info:eu-repo/dai'
    }


def xpath(elt, path):
    return elt.xpath(path, namespaces=namespaces)


def read_dai_mapping(fname):
    res = {}
    with open(fname) as f:
        for line in f:
            dai, authority, author_id = [e.strip() for e in line.split("\t")]
            res[author_id] = dai

    return res


fname, dai_mapping_fname = sys.argv[1], sys.argv[2]
dai_mapping = read_dai_mapping(dai_mapping_fname)
doc = etree.parse(fname)

count =10
for name in xpath(doc, '//mods:name[@type="personal"]'):
    try:
        author_id = unicode(name.attrib['ID'])
        author_display = xpath(name, 'normalize-space(mods:displayForm)')
        author_given = xpath(name, 'normalize-space(mods:namePart[@type="given"])')
        author_family = xpath(name, 'normalize-space(mods:namePart[@type="family"])')
        dai = dai_mapping.get(author_id, 'no_dai_for_you')

    except KeyError:
        print >> sys.stderr, "name has no ID: ", etree.tostring(name)
        continue

    print "\t".join(unicode(e).encode("utf-8") for e in [dai, author_id, author_family, author_given, author_display])
