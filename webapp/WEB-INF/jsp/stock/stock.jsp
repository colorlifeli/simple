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
	
		<input type="button" value="实时计算" onclick="openWin('pwin');"/>
		<input type="button" value="写入数据库" onclick="writeToDB();"/>
		<br/>
		<input type="checkbox" id="isFromDB" name="isFromDB">从数据库

		<form name="fm1" id="fm1">

			<table class="fix_table">
				<tr>
					<td class="bgc_tt short">代码</td>
					<td class="long"><input name="code" id="code" type="text" size="10">
					</td>
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
		
		<br/><input type="button" value="某天全卖" onclick="sellAll();"/>
		<br/><span id="sellAll"></span>
	</div>
	
	<div id="center_div" region="center" style="width:100%;height:100%;">
	
		<div id="tabs" class="easyui-tabs" >
			<div id="tab1" title="详情">	
				<table id="codeOperTable"></table>
			</div>
			<div id="tab2" title="k"></div> 
		</div>
	</div>

	
	
	<div id="bar" class="easyui-dialog" title="处理中" data-options="closed:true">
		<img src="<%=ctx%>/image/progressbar.gif"/>
	</div>
	
	<!-- 参数配置窗口 -->
	<div id="pwin" class="easyui-window" title="计算" data-options="iconCls:'icon-save',closed:true"
		style="width: 370px; height: 200px; left: 380; top: 230;">
		<form name="pform" id="pform" method="post">
			<table>
				<tr>
					<td>定价策略</td>
					<td><input name="priceStrategy" id="priceStrategy" type="text" size="10" value="1"></input>
					</td>
				</tr>
				<tr>
					<td>开始日期</td>
					<td><input name="startDate" id="startDate" type="text" size="10" value="2014-04-01"></input>
					</td>
				</tr>
				
				<tr>
					<td align="right" style="width: 100px"><input type="button" value="开始计算" onclick="compute();">
					</td>
					<td><input type="button" class="button_ty" value="取消" onclick="closeWin('pwin');"></td>
				</tr>
				
			</table>
		</form>
	</div>

</body>
</html>