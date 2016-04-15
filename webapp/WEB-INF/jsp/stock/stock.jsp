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
		<input type="checkbox" id="isFromDB" name="isFromDB">从数据库

		<form name="fm1" id="fm1">

			<table class="fix_table">
				<tr>
					<td class="bgc_tt short">名称</td>
					<td class="long"><input name="name" id="name" type="text" size="10">
					</td>
					<td class="bgc_tt short">0次数</td>
					<td class="long"><input name="times" id="times" type="text" size="10">
					</td>
					<td class="bgc_tt short">win次数</td>
					<td class="long"><input name="winTimes" id="winTimes" type="text" size="10">
					</td>
				</tr>
				<tr>
					<td class="bgc_tt short">lose次数</td>
					<td class="long"><input name="loseTimes" id="loseTimes" type="text" size="10">
					</td>
					<td class="bgc_tt short">last余额</td>
					<td class="long"><input name="lastRemain" id="lastRemain" type="text" size="10">
					</td>
					<td class="bgc_tt short">min余额</td>
					<td class="long"><input name="minRemain" id="minRemain" type="text" size="10">
					</td>
				</tr>

				<tr>
					<td colspan="4" align="center"><input type="button" value="查询"
						onclick="getOperSum();" /> <input type="reset" class="button_ty"
						value="重置"></td>
				</tr>
			</table>
		</form>

		<table id="allTable"></table>
		
		<br/><input type="button" value="所有汇总数据总结" onclick="summary();"/>
		<br/><span id="summary"></span>
	</div>
	
	<div id="center_div" region="center" >
	
		<div id="tabs" class="easyui-tabs" >
			<div id="tab1" title="详情">	
				<table id="codeOperTable"></table>
			</div>
			<div id="tab2" title="k" href="kkk"></div>
		</div>
	</div>

	
	
	<div id="bar" class="easyui-dialog" title="处理中">
		<img src="<%=ctx%>/image/progressbar.gif"/>
	</div>

</body>
</html>