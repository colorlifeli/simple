<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/jsp/common/head.jsp"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="">
    <meta name="author" content="">
    
    <link rel="stylesheet" href="<%=ctx %>/js/bootstrap-3.3.5-dist/css/bootstrap.min.css">
    <link href="<%=ctx %>/js/sb-admin/sb-admin-2.css" rel="stylesheet">
    <link href="<%=ctx %>/js/metisMenu/metisMenu.min.css" rel="stylesheet">
    
	
	<title>Admin</title>
</head>
<body>

	<div id="wrapper">
		<nav class="navbar navbar-default navbar-static-top" role="navigation"
			style="margin-bottom: 0"> 
			
			<!-- 左上角  -->
			<div class="navbar-header">
                <button type="button" class="navbar-toggle" data-toggle="collapse" data-target=".navbar-collapse">
                    <span class="sr-only">切换导航</span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                    <span class="icon-bar"></span>
                </button>
                <a class="navbar-brand" href="index.jsp">后台管理 v2.0</a>
            </div>
            
            <!-- ***************       上侧导航            ********************* -->
            <ul class="nav navbar-top-links navbar-right"> <!-- 放在右侧 -->
                <li class="dropdown">
                	<!-- data-toggle 表示要做什么 -->
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="glyphicon glyphicon-envelope"></i>  <i class="glyphicon glyphicon-chevron-down"></i>
                        <!-- 信封图标，以及向下箭头 -->
                    </a>
                    <ul class="dropdown-menu dropdown-messages">
                        <li>
                            <a href="#">
                                <div>
                                    <strong>John Smith</strong>
                                    <span class="pull-right text-muted">
                                        <em>Yesterday</em>
                                    </span>
                                </div>
                                <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eleifend...</div>
                            </a>
                        </li>
                        <!-- 分隔线 -->
                        <li class="divider"></li>
                        <li>
                            <a href="#">
                                <div>
                                    <strong>John Smith</strong>
                                    <span class="pull-right text-muted">
                                        <em>Yesterday</em>
                                    </span>
                                </div>
                                <div>Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque eleifend...</div>
                            </a>
                        </li>
                        <li class="divider"></li>
                        <li>
                            <a class="text-center" href="#">
                                <strong>Read All Messages</strong>
                                <i class="fa fa-angle-right"></i>
                            </a>
                        </li>
                    </ul>
                    <!-- /.dropdown-messages -->
                </li>
                <!-- 进度条示例 -->
                <li class="dropdown">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="glyphicon glyphicon-calendar"></i>  <i class="glyphicon glyphicon-chevron-down"></i>
                    </a>
                    <ul class="dropdown-menu dropdown-tasks">
                        <li>
                            <a href="#">
                                <div>
                                    <p>
                                        <strong>Task 1</strong>
                                        <span class="pull-right text-muted">40% Complete</span>
                                    </p>
                                    <div class="progress progress-striped active">
                                        <div class="progress-bar progress-bar-success" role="progressbar" aria-valuenow="40" aria-valuemin="0" aria-valuemax="100" style="width: 40%">
                                            <span class="sr-only">40% Complete (success)</span>
                                        </div>
                                    </div>
                                </div>
                            </a>
                        </li>                       
                    </ul>
                    <!-- /.dropdown-tasks -->
                </li>
                
                <!-- 设置 -->
                <!-- /.dropdown -->
                <li class="dropdown">
                    <a class="dropdown-toggle" data-toggle="dropdown" href="#">
                        <i class="glyphicon glyphicon-time"></i>  <i class="glyphicon glyphicon-chevron-down"></i>
                    </a>
                    <ul class="dropdown-menu dropdown-user">
                        <li><a href="#"><i class="fa fa-user fa-fw"></i> 用户资料</a>
                        </li>
                        <li><a href="#"><i class="fa fa-gear fa-fw"></i> 设置</a>
                        </li>
                        <li class="divider"></li>
                        <li><a href="login.html"><i class="fa fa-sign-out fa-fw"></i> 退出</a>
                        </li>
                    </ul>
                    <!-- /.dropdown-user -->
                </li>
                <!-- /.dropdown -->
            </ul>
            <!-- ***************       上侧导航   END        ********************* -->

 			<!-- ***************       左侧导航            ********************* -->
            <div class="navbar-default sidebar" role="navigation">
                <div class="sidebar-nav navbar-collapse">
                    <ul class="nav" id="side-menu">
                    
                    	<!-- 搜索框 -->
                        <li class="sidebar-search">
                            <div class="input-group custom-search-form">
                                <input type="text" class="form-control" placeholder="查找...">
                                <span class="input-group-btn">
                                <button class="btn btn-default" type="button">
                                    <i class="glyphicon glyphicon-search"></i>
                                </button>
                            </span>
                            </div>
                        </li>
                        <li>
                            <a href="index.jsp"><i class="fa fa-dashboard fa-fw"></i> 首页</a>
                        </li>
                        
                        <li>
                            <a href="tables.html"><i class="fa fa-table fa-fw"></i> 表格标例</a>
                        </li>
                        <li>
                            <a href="forms.html"><i class="fa fa-edit fa-fw"></i> 表单标例</a>
                        </li>
                        <li>
                            <a href="#"><i class="fa fa-wrench fa-fw"></i> UI 元素<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="panels-wells.html">Panels and Wells</a>
                                </li>
                                <li>
                                    <a href="buttons.html">按钮</a>
                                </li>
                                <li>
                                    <a href="notifications.html">通知</a>
                                </li>
                                <li>
                                    <a href="typography.html">排版</a>
                                </li>
                                <li>
                                    <a href="icons.html"> 图标</a>
                                </li>
                                <li>
                                    <a href="grid.html">网格</a>
                                </li>
                            </ul>
                            <!-- /.nav-second-level -->
                        </li>
                        
                        <li>
                            <a href="#"><i class="fa fa-files-o fa-fw"></i> 样例页面<span class="fa arrow"></span></a>
                            <ul class="nav nav-second-level">
                                <li>
                                    <a href="login.html">登录页面</a>
                                </li>
                            </ul>
                        </li>
                    </ul>
                </div>
                <!-- /.sidebar-collapse -->
            </div>
            </nav>
            
            <!-- ***************       页面内容            ********************* -->
            <div id="page-wrapper">
            
            </div>
            
	</div>

	<script src="<%=ctx %>/js/bootstrap-3.3.5-dist/js/bootstrap.min.js"></script>
<script src="<%=ctx %>/js/sb-admin/sb-admin-2.js"></script>
<script src="<%=ctx %>/js/metisMenu/metisMenu.min.js"></script>
</body>
</html>