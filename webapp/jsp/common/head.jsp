<%  
	String ctx = request.getContextPath(); 
 	String basePath=request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+ctx+"/";
 %>

<script>
var contextPath = "<%=request.getContextPath()%>";
var basePath = "<%=basePath%>";
</script>

<!-- 
<link rel="stylesheet" type="text/css" href="<%=ctx %>/js/jquery-easyui-1.4.3/themes/icon.css">
<script type="text/javascript" src="<%=ctx %>/js/jquery-easyui-1.4.3/jquery.easyloader.js"></script>
 -->
<script type="text/javascript" src="<%=ctx %>/js/jquery-2.1.4.min.js"></script>

<head>
	<base href="<%=basePath%>">
</head>
