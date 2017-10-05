[#-- @ftlvariable name="action" type="com.go2group.actions.HipChatConfigurationPlugin" --]
[#-- @ftlvariable name="" type="com.go2group.actions.HipChatConfigurationPlugin" --]

<html>
<head>
	<title>[@ww.text name='hipchat-config.name' /]</title>
    <meta name="decorator" content="adminpage">
</head>
<body>

<h1>[@ww.text name='hipchat-config.name' /]</h1>

[@ww.form action="updateConfigureHipchatPlugin"
        namespace="/admin"
        titleKey="hipchat.global.config"
        submitLabelKey='global.buttons.update']
        [#if token?has_content]
        [@ww.param name='buttons']
            <a href="${req.contextPath}/admin/viewAnnouncements.action">&nbsp;Announcements</a>
        [/@ww.param]
        [/#if]
        [@ui.bambooSection]
            [@ww.textfield name='serverUrl' labelKey='admin.server.label' descriptionKey='admin.server.description'/]
        [/@ui.bambooSection]
        [@ui.bambooSection]
            [@ww.textfield name='token' labelKey='admin.token.label' descriptionKey='admin.token.description'/]
        [/@ui.bambooSection]
[/@ww.form]
