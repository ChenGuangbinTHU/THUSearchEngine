<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
    request.setCharacterEncoding("utf-8");
    response.setCharacterEncoding("utf-8");
    String path = request.getContextPath();
//    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
//    String imagePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+"/";
    String currentQuery = (String) request.getAttribute("currentQuery");
    int currentPage = (Integer) request.getAttribute("currentPage");
    int pageNum = (Integer) request.getAttribute("pageNum");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Search Result</title>
    <link rel="stylesheet" type="text/css" href="<%=path%>/lib/semantic/dist/semantic.min.css">
    <style type="text/css">
        #news-items em {
            color: #C00000;
            font-style: normal;
        }
    </style>
</head>
<body>

<div class="ui vertical basic segment">
	<div class="ui stackable grid container">
		<div class="row">
			<div class="right aligned two wide column">
				<img src="<%=path%>/images/logo.png" alt="logo.png">
			</div>
			<div class="ten wide column">
                <form action="ImageServer" method="get" class="submit-form">
                    <div class="ui fluid action input">
                        <input type="text" name="query" placeholder="Search..." value="<%=currentQuery%>" class="search-input">
                        <button class="ui purple button" type="submit">Search</button>
                    </div>
                </form>
			</div>
			<div class="four wide column"></div>
		</div>
		<div class="row">
			<div class="two wide column"></div>
			<div class="ten wide column">
				<div class="ui divided items" id="news-items">
                    <%
                        String[] urls = (String[]) request.getAttribute("urls");
                        String[] texts = (String[]) request.getAttribute("texts");
                        String[] titles = (String[]) request.getAttribute("titles");
                        if ((urls != null) && (urls.length > 0)) {
                            for (int i = 0; i < urls.length; ++i) {
//                                if (titles[i].contains(" : ")) {
//                                    titles[i] = titles[i].split(" : ")[1];
//                                }
                                if (titles[i].length() > 42) {
                                    titles[i] = titles[i].substring(0, 41) + "...";
                                }
                                StringBuilder newTitle = new StringBuilder();
                                for (int j = 0; j < titles[i].length(); ++j) {
                                    String s = titles[i].substring(j, j + 1);
                                    if (currentQuery.contains(s)) {
                                        newTitle.append("<em>").append(s).append("</em>");
                                    } else {
                                        newTitle.append(s);
                                    }
                                }
                                titles[i] = newTitle.toString();
                                int firstIndex = 0;
                                for (int j = 0; j < texts[i].length(); ++j) {
                                    if (currentQuery.contains(texts[i].substring(j, j + 1))) {
                                        firstIndex = j;
                                        break;
                                    }
                                }
                                int comma = texts[i].lastIndexOf("，", firstIndex);
                                int period = texts[i].lastIndexOf("。", firstIndex);
                                int space = texts[i].lastIndexOf(" ", firstIndex);
                                comma = ((comma == -1) ? 0 : comma);
                                period = ((period == -1) ? 0 : period);
                                space = ((space == -1) ? 0 : space);
                                firstIndex = Math.max(comma, Math.max(period, space));
                                texts[i] = texts[i].substring(firstIndex);
                                if (texts[i].length() > 120) {
                                    texts[i] = texts[i].substring(0, 118) + "...";
                                }
                                StringBuilder newText = new StringBuilder();
                                for (int j = 0; j < texts[i].length(); ++j) {
                                    String s = texts[i].substring(j, j + 1);
                                    if (currentQuery.contains(s)) {
                                        newText.append("<em>").append(s).append("</em>");
                                    } else {
                                        newText.append(s);
                                    }
                                }
                                texts[i] = newText.toString();
                                String splitUrl = urls[i].split("/")[0];
                    %>
                    <div class="item">
                        <div class="content">
                            <a href="http://<%=urls[i]%>" class="header" style="color: #0021CB;"><%=titles[i]%></a>
                            <div class="description"><p><%=texts[i]%></p></div>
                            <div class="meta">
                                <a href="http://<%=urls[i]%>" style="color: #008000;"><%=splitUrl%></a>
                            </div>
                        </div>
                    </div>
                    <%
                            }
                        } else {
                    %>
                    <h1>No Search Result</h1>
                    <%
                        }
                    %>
				</div>

                <div class="ui pagination menu" id="jump-bar"></div>
			</div>
			<div class="four wide column"></div>
		</div>
        <div class="row">
            <div class="two wide column"></div>
            <div class="ten wide column">
                <form action="ImageServer" method="get" class="submit-form">
                    <div class="ui fluid action input">
                        <input type="text" name="query" placeholder="Search..." value="<%=currentQuery%>" class="search-input">
                        <button class="ui purple button" type="submit">Search</button>
                    </div>
                </form>
            </div>
            <div class="four wide column"></div>
        </div>
	</div>
</div>

<script type="text/javascript" src="<%=path%>/lib/jquery-3.2.1/jquery-3.2.1.min.js"></script>
<script type="text/javascript" src="<%=path%>/lib/semantic/dist/semantic.min.js"></script>
<script type="text/javascript">
    $(function () {
        $('.submit-form').submit(function (event) {
            if ($('.search-input').val().length <= 0) {
                event.preventDefault();
            }
        });

        setJBar(<%=currentPage%>, <%=pageNum%>);
    });

    function setJBar(page_id, length) {
        var jbar = $('#jump-bar');
        page_id = parseInt(page_id);
        jbar.children().remove();
        jbar.append(''
            + '<a class="icon item left jitem" style=>'
                + '<i class="fast backward icon"></i>'
            + '</a>'
        );
        if (page_id <= 1) {
            jbar.children('.left').addClass('disabled');
        }
        if ((page_id > 0) && (length > 0)) {
            if ((page_id >= 9) && (page_id === length)) {
                jbar.append('<a class="item jitem">' + (page_id - 8) + '</i>');
            }
            if ((page_id >= 8) && (page_id + 1 >= length)) {
                jbar.append('<a class="item jitem">' + (page_id - 7) + '</i>');
            }
            if ((page_id >= 7) && (page_id + 2 >= length)) {
                jbar.append('<a class="item jitem">' + (page_id - 6) + '</i>');
            }
            if ((page_id >= 6) && (page_id + 3 >= length)) {
                jbar.append('<a class="item jitem">' + (page_id - 5) + '</i>');
            }
            if (page_id >= 5) {
                jbar.append('<a class="item jitem">' + (page_id - 4) + '</i>');
            }
            if (page_id >= 4) {
                jbar.append('<a class="item jitem">' + (page_id - 3) + '</i>');
            }
            if (page_id >= 3) {
                jbar.append('<a class="item jitem">' + (page_id - 2) + '</i>');
            }
            if (page_id >= 2) {
                jbar.append('<a class="item jitem">' + (page_id - 1) + '</i>');
            }
            if (page_id >= 1) {
                jbar.append('<a class="disabled active item jitem" style="font-weight: bold">' + page_id + '</i>');
            }
            if (page_id + 1 <= length) {
                jbar.append('<a class="item jitem">' + (page_id + 1) + '</i>');
            }
            if (page_id + 2 <= length) {
                jbar.append('<a class="item jitem">' + (page_id + 2) + '</i>');
            }
            if (page_id + 3 <= length) {
                jbar.append('<a class="item jitem">' + (page_id + 3) + '</i>');
            }
            if (page_id + 4 <= length) {
                jbar.append('<a class="item jitem">' + (page_id + 4) + '</i>');
            }

            if ((page_id + 5 <= length) && (page_id <= 4)) {
                jbar.append('<a class="item jitem">' + (page_id + 5) + '</i>');
            }
            if ((page_id + 6 <= length) && (page_id <= 3)) {
                jbar.append('<a class="item jitem">' + (page_id + 6) + '</i>');
            }
            if ((page_id + 7 <= length) && (page_id <= 2)) {
                jbar.append('<a class="item jitem">' + (page_id + 7) + '</i>');
            }
            if ((page_id + 8 <= length) && (page_id === 1)) {
                jbar.append('<a class="item jitem">' + (page_id + 8) + '</i>');
            }
        }
        jbar.append(''
            + '<a class="icon item right jitem">'
                + '<i class="fast forward icon"></i>'
            + '</a>'
        );
        if ((page_id >= length) || (page_id === 0)) {
            jbar.children('.right').addClass('disabled');
        }
        $('.jitem').click(function () {
            window.location.href = 'ImageServer?query=<%=currentQuery%>&page=' + ($(this).hasClass('left') ? 1 : ($(this).hasClass('right') ? <%=pageNum%> : $(this).text()));
        })
    }
</script>
</body>
</html>