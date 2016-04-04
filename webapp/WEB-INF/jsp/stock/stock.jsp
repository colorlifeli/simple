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

	<div id="westDiv" style="width: 500px;" region="west">
	
	<input type="button" value="compute" onclick="compute();"/>
		<table id="allTable"></table>
	</div>

	<div id="eastDiv" style="width: 500px;" region="east" border="true">
	
	</div>

</body>
</html>