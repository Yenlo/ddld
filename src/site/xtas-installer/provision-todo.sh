# TODOs:

# rabbitmq

sudo zypper --non-interactive install rabbitmq-server

systemctl start rabbitmq-server

sudo chkconfig rabbitmq-server on

# xtas server and worker, and all of its dependencies

sudo zypper --non-interactive remove patterns-openSUSE-minimal_base-conflicts

sudo zypper --non-interactive install \
	gcc \
	git \
	libxml2-devel \
	libxslt-devel \
	python-devel \
	python-pip \
	python-numpy \
	python-scipy \
	python-virtualenv \
	python-distribute \
	python-libxml2  \
	libxslt-python

sudo pip install \
	gensim \
	django-cms \
	south \
	pymongo \
	celery \
	django-celery \
	django-documentation \
	lxml \
	twisted \
	simplejson \
	nltk

sudo pip install git+https://github.com/orooij/xtas.git # or https://github.com/NLeSC/xtas.git

cd ~

python -m xtas.configure > xtas.yaml

nohup python -m xtas.worker &

sudo nohup python -m xtas.server &
