<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/jsp/common/easyuiHead.jsp"%>

<script language="JavaScript" src="<%=ctx%>/jsp/js/dbtool.js"></script>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>test</title>
</head>
<body class="easyui-layout">

	<div id="divTableList" style="width: 240px;" region="west">

		<input class="easyui-combobox" id="pattern" name="pattern"
			data-options="valueField:'tableName',textField:'tableName'" />
		<input type="button" value="查询" onclick="getTableNames();" /> <br />
		<table id="allTable"></table>

	</div>



	<div id="divTableDetail" class="easyui-layout" region="center"
		style="width: 100%; height: 100%;">

		<div id="divCondition" style="width: 300px;" region="west">


			<div id="condition">
				<form id="formCondition">
					<table id="conditionTable" class="fix_table">
					</table>
				</form>
				<input type="hidden" id="tableName" name="tableName"/>
			</div>

			<br />
			<input type="button" value="查询" onclick="queryTable();" /> <br />
		</div>

		<div id="divData" region="center" style="width: 100%; height: 100%;">
			<table id="data"></table>
		</div>
	</div>


</body>
</html>