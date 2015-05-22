import sys
import os
import errno

import requests
from lxml import etree
from pprint import pprint
import pytz
from datetime import datetime


repo = 'narcis'

NAMESPACES = {
    'm': "http://www.loc.gov/mods/v3",
    "oai": "http://www.openarchives.org/OAI/2.0/"
    }

oai_endpoints = {
    'uva': 'http://dare.uva.nl/cgi/arno/oai/uvapub',
    'narcis': 'http://oai.narcis.nl/oai'
    }

oai_params = {
    'uva': {'verb': 'ListRecords',
              # 'set': 'narcis', # hivos, hvalup, keur, kit-withfulltext, narcis, aopen, scholar, withfulltext
              # ('metadataPrefix','nl_didl'),
              'metadataPrefix': 'oai_dc' #oai_dc, nl_didl, arno
            },
    'narcis': {'verb': 'ListRecords',
               # 'set': 'narcis', # hivos, hvalup, keur, kit-withfulltext, narcis, aopen, scholar, withfulltext
               'metadataPrefix': 'oai_dc' # narcis only provides oai_dc
               }
    }


class OAIArchiver(object):
    BASEDIR = 'output'
    def __init__(self, repo, format):
        self.repo = repo
        self.format = format
        self.sequence = 0
        self.start = None
        self.output_dir = None

    def write(self, data):
        if not self.start:
            self.start = nu()

        if not self.output_dir:
            self.output_dir = self.mkdir()

        with open("%s/%d.xml" % (self.output_dir, self.sequence), 'w') as out:
            out.write(text.encode("utf-8"))
        self.sequence += 1

    def mkdir(self):
        """Do `mkdir -p` and return the name of the path created.
        """
        dir_name = os.path.join(OAIArchiver.BASEDIR,
                                self.repo,
                                self.format,
                                self.start.isoformat())
        try:
            os.makedirs(dir_name)
        except OSError as exc: # Python >2.5
            if exc.errno == errno.EEXIST and os.path.isdir(path):
                pass
            else:
                raise

        return dir_name


def nu(tz=None):
    """ Returns a 'now' datetime with microseconds set to zero.

    Timezone can be specified with the optional `tz` argument, the
    timezone defaults to `pytz.utc`.
    """
    tz = tz if tz else pytz.utc
    now = datetime.now(tz).replace(microsecond=0)
    return now


def xpath(elt, path):
    return elt.xpath(path, namespaces=NAMESPACES)


# def write_partial_record_list(repo, format, seq, text, dt_start):
#     dir_name = "output/%s.%s/%s" % (repo, format, dt_start.isoformat())
#     with open("%s/%d.xml" % (dir_name, seq), 'w') as out:
#         out.write(text.encode("utf-8"))


archiver = OAIArchiver(repo, oai_params[repo]['metadataPrefix'])
resp = requests.get(oai_endpoints[repo], params=oai_params[repo])
resp.encoding = 'utf-8'


text = resp.text
doc = etree.fromstring(text.encode("utf-8"))

# write_partial_record_list(repo, oai_params[repo]['metadataPrefix'], seq, text, dt_start)
archiver.write(text)

resumption_token = xpath(doc, 'string(//oai:resumptionToken)')
num_records = xpath(doc, 'count(//oai:record)')

while resumption_token:
    print >> sys.stderr, "%s fetched %d records" % (nu().isoformat(), num_records)
    params = {'resumptionToken': resumption_token, 'verb': 'ListRecords'}
    resp = requests.get(oai_endpoints[repo], params=params)
    resp.encoding = 'utf-8'

    text = resp.text
    doc = etree.fromstring(text.encode("utf-8"))

    # write_partial_record_list(repo, oai_params[repo]['metadataPrefix'], seq, text, dt_start)
    archiver.write(text)

    resumption_token = xpath(doc, 'string(//oai:resumptionToken)')
    num_records += xpath(doc, 'count(//oai:record)')

print >> sys.stderr, "%s fetched %d records" % (nu().isoformat(), num_records)
