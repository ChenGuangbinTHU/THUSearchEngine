<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    request.setCharacterEncoding("utf-8");
    System.out.println(request.getCharacterEncoding());
    response.setCharacterEncoding("utf-8");
    System.out.println(response.getCharacterEncoding());
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    System.out.println(path);
    System.out.println(basePath);
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Search</title>
    <link rel="stylesheet" type="text/css" href="lib/semantic/dist/semantic.min.css">
    <style type="text/css">
        #background-picture {
            position: relative;
            background: url(<%=path%>/images/thu.jpg) no-repeat;
            background-size: cover;
            height: 100%;
            width: 100%;
        }
    </style>
</head>
<body>

<div id="background-picture">
    <div class="ui card" id="search-card">
        <div class="ui dimmer" id="loader">
            <div class="ui loader"></div>
        </div>
        <div class="content">
            <div class="center aligned header">THU Search</div>
        </div>
        <div class="content">
            <form action="servlet/ImageServer" method="get" id="submit-form">
                <div class="ui fluid action input">
                    <input type="text" name="query" placeholder="Search..." id="search-input">
                    <button class="ui purple button" type="submit">Search</button>
                </div>
            </form>
        </div>
    </div>
</div>

<script type="text/javascript" src="<%=path%>/lib/jquery-3.2.1/jquery-3.2.1.min.js"></script>
<script type="text/javascript" src="<%=path%>lib/semantic/dist/semantic.min.js"></script>
<script type="text/javascript">
    $(function () {
        $('#submit-form').submit(function (event) {
            if ($('#search-input').val().length <= 0) {
                event.preventDefault();
            }
        });

        setCard();
        $(window).resize(setCard);
    });

    function setCard() {
        var card = $('#search-card');
        var h = $(window).height(), w = $(window).width();
        card.css('width', w * 0.5).css('left', w / 4).css('top', h / 3 - card.height());
    }
</script>

</body>
</html>