<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/jsp/common/easyuiHead.jsp"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>主页</title>
<script type="text/javascript">
function to(title, target){
	var url = basePath + target;
	 var content = '<iframe scrolling="auto" frameborder="0" src="'+url+'" style="width:100%;height:100%;"></iframe>';
	$('#mainTabs').tabs('add',{
	    title:title,
	    content:content,
	    closable:true,
	    //href:basePath + 'stock/enter',
	    tools:[{
			iconCls:'icon-mini-refresh',
			handler:function(){
				alert('refresh');
			}
	    }]
	});
}
</script>

</head>
<body class="easyui-layout" data-options="fit:true">
 
	<div data-options="region:'north'" style="height:30px">
	<a href="#" onclick="to('stock','stock/enter')" style="font-size:14px;display:inline-block;text-align:bottom">stock</a>
	 | <a href="#" onclick="to('dbtool','db/enter')" style="font-size:14px;display:inline-block;text-align:bottom">dbtool</a>
	</div>
    <div id="main" data-options="region:'center',fit:true" >
    	<div id="mainTabs" class="easyui-tabs" data-options="fit:true">
		</div>
    </div>

</body>
</html>