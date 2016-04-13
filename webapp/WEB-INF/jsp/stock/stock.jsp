<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/jsp/common/easyuiHead.jsp"%>

<script language="JavaScript" src="<%=ctx%>/jsp/js/stock.js"></script>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>test</title>
</head>
<body class="easyui-layout">

	<div id="westDiv" style="width: 600px;" region="west">
	
		<input type="button" value="实时计算" onclick="compute();"/>
		<input type="button" value="写入数据库" onclick="writeToDB();"/>
		<br/>
		<input type="checkbox" id="isFromDB" >从数据库
		<input type="button" value="查询" onclick="getOperSum();"/>
		
		<table id="allTable"></table>
	</div>
	
	<div id="center_div" region="center" >
		<table id="codeOperTable"></table>
	</div>

	
	
	<div id="bar" class="easyui-dialog" title="处理中">
		<img src="<%=ctx%>/image/progressbar.gif"/>
	</div>

</body>
</html>