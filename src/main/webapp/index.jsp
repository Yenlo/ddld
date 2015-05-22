<!doctype html>
<html>
	<head>
		<title>DLD</title>
		<meta name="fragment" content="!">
		<meta name="viewport" content="width=device-width">
		<link rel="stylesheet" href="/static/css/lib/bootstrap.min.css">
		<link rel="stylesheet" href="/static/css/lib/font-awesome.css">
		<link rel="stylesheet" href="/static/css/style.css">
		<link rel="icon" href="/static/logos/favicon.ico" type="image/x-icon"/>
		<script type="text/javascript" src="/static/js/lib/jquery.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/bootstrap.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/angular.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/angular-route.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/angular-resource.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/angular-animate.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/ui-bootstrap-tpls-0.10.0.min.js"></script>
		<script type="text/javascript" src="/static/js/lib/store.js"></script>
		<script type="text/javascript" src="/static/js/lib/truncate.js"></script>
		<script type="text/javascript" src="/static/js/api.js"></script>
		<script type="text/javascript" src="/static/js/history.js"></script>
		<script type="text/javascript" src="/static/js/notifications.js"></script>
		<script type="text/javascript" src="/static/js/controller.js"></script>
	</head>
	<body data-ng-app="ddld" data-ng-controller="MainController" data-ng-class="{ sidebar: is('/app/sidebar')}">
		<div class="navbar navbar-fixed-top navbar-inverse" data-ng-hide="is('/app/sidebar')">
			<div class="container">
				<div class="navbar-header">
					<button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
						<span class="icon-bar"></span>
					</button>
					<a class="navbar-brand" href="/">DLD</a>
				</div>
				<div class="collapse navbar-collapse">
					<ul class="nav navbar-nav">
						<li data-ng-class="{ active: is('/app/search')}"><a data-ng-show="isLoggedin()" href="/app/search">Factcheck</a></li>
						<li data-ng-class="{ active: is('/app/tree')}"><a data-ng-show="isLoggedin()" href="/app/tree">Geschiedenis</a></li>
						<li data-ng-class="{ active: is('/app/push')}"><a data-ng-show="isLoggedin()" href="/app/push">Push</a></li>
						<!--li data-ng-class="{ active: is('/app/push')}"><a data-ng-show="isLoggedin() && isAdmin()" href="/app/sources">Push</a></li -->
						<!--li data-ng-class="{ active: is('/app/sources')}"><a data-ng-show="isLoggedin() && isAdmin()" href="/app/sources">Bronnen</a></li -->
						<!--li data-ng-class="{ active: is('/app/profile')}"><a data-ng-show="isLoggedin() && isAdmin()" href="/app/profile">Profielen</a></li-->
						<!--li data-ng-class="{ active: is('/app/users')}"><a data-ng-show="isLoggedin() && isAdmin()" href="/app/users">Organisatie</a></li-->
					</ul>
					<ul class="nav navbar-nav pull-right">
						<li class="nav-custom">{{ getUsername() }}</li>
						<li data-ng-class="{ active: is('/app/login')}"><a data-ng-hide="isLoggedin()" href="/app/login">Login</a></li>
						<li data-ng-class="{ active: is('/app/register')}"><a data-ng-hide="isLoggedin()" href="/app/register">Registreer</a></li>
						<li><a data-ng-show="isLoggedin()" data-ng-click="logout()">Logout</a></li>
					</ul>
				</div>
			</div>
		</div>
		<div class="container-fluid" data-ng-cloak>
			<div class="row">
				<div class="col-lg-2" data-ng-show="isLoggedin()" data-ng-controller="PushSidebarController">
					<h5 data-ng-hide="is('/app/sidebar')"><a href="/app/push">Push Bronnen</a></h5>
					<h5 data-ng-show="is('/app/sidebar')">Push Bronnen</h5>
		            <p data-ng-show="crawlers.length == 0 && loadState == 'loaded'">Momenteel geen bronnen</p>
					<img data-ng-show="crawlers.length == 0 && loadState == 'loading'" class="img-responsive center-block" src="/static/imagery/loader.gif" width="64px" height="64px"/>
		            <p data-ng-repeat="crawler in crawlers" class="clearfix">
		              {{ crawler.title.length > 0 ? crawler.title : crawler.url }}
		              <span class="pull-right" data-ng-show="crawlHits[crawler.id]">
					    <span class="label label-info" data-ng-click="openCrawlerModal(crawler, false)">{{ crawlHits[crawler.id] }}</span>
		                <span class="label label-success" data-ng-click="openCrawlerModal(crawler, true)">{{ crawlHighlights[crawler.id] }}</span>
		              </span>
		            </p>
		            <hr/>
			   		<div class="modal fade" id="pushModalController" tabindex="-1" role="dialog">
						<div class="modal-dialog">
							<div class="modal-content">
								<div class="modal-header">
									<button type="button" class="close" data-dismiss="modal">&times;</button>
									<h4 class="modal-title">Push Bron - {{ selected.title }} {{ highlights ? "( Alleen highlights )" : "" }}</h4>
								</div>
								<div class="modal-body">
									<div data-ng-show="paginationState == 'loading'">
										<img class="img-responsive center-block" src="/static/imagery/loader.gif" width="64px" height="64px"/>
									</div>
									<div data-ng-show="paginationState == 'loaded'">
										<div data-ng-repeat="result in selectedResults">
											<a data-ng-href="{{ result.url }}" target="_blank">{{ result.title }} <!-- {{ result.timestamp }}  --></a>
										</div>
									</div>
									<pagination total-items="currentItems" page="currentPage" max-size="maxSize" class="pagination-sm" boundary-links="true"
												previous-text="&lsaquo;" next-text="&rsaquo;" first-text="&laquo;" last-text="&raquo;"></pagination>
								</div>
								<div class="modal-footer">
									<button type="button" class="btn btn-default" data-dismiss="modal">Sluiten</button>
								</div>
							</div>
						</div>
					</div>
				</div>
				<div data-ng-class="{true:'col-lg-8', false:'col-lg-8 col-lg-offset-2'}[isLoggedin()]" data-ng-view></div>
			</div>
		</div>
		<div data-alert-listener style="position: fixed; bottom: 0; right: 20px;"></div>
	</body>
</html>