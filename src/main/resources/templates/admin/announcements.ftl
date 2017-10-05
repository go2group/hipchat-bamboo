[#-- @ftlvariable name="action" type="com.go2group.actions.Announcements" --]
[#-- @ftlvariable name="" type="com.go2group.actions.Announcements" --]

<html>
<head>
	<title>[@ww.text name='hipchat-announcements.name' /]</title>
    <meta name="decorator" content="adminpage">
</head>
<body>

<h1>[@ww.text name='hipchat-announcements.name' /]</h1>

[@ww.form action="postAnnouncements"
        namespace="/admin"
        titleKey="hipchat-announcements.name"
        submitLabelKey='hipchat-announcements.post']
        [@ww.param name='buttons']
            <a href="${req.contextPath}/admin/viewConfigureHipchatPlugin.action">&nbsp;Cancel</a>
        [/@ww.param]
        [@ui.bambooSection]
            [@ww.hidden name="token" value=$token /]
            [@ww.hidden name="serverUrl" value=$serverUrl /]
            [@ww.textarea labelKey='announcement.name' name="announcement" rows="5" cssClass="long-field" /]
        	<fieldset id="fieldArea_postAnnouncements_color" class="group">
        		<legend id="fieldLabelArea_postAnnouncements_color_legend">
					<span>Color</span>
				</legend>
        		<div class="radio">
		        	[@ww.radio name='color' template="radio.ftl" theme="simple"  fieldValue='yellow'/]
		            [@ww.text name='Yellow' /]
		            [@ww.radio name='color' template="radio.ftl" theme="simple"  fieldValue='red'/]
		            [@ww.text name='Red' /]
		            [@ww.radio name='color' template="radio.ftl" theme="simple"  fieldValue='green'/]
		            [@ww.text name='Green' /]
		            [@ww.radio name='color' template="radio.ftl" theme="simple"  fieldValue='purple'/]
		            [@ww.text name='Purple' /]
		            [@ww.radio name='color' template="radio.ftl" theme="simple"  fieldValue='gray'/]
		            [@ww.text name='Gray' /]
	            </div>
	        </fieldset>
	        <fieldset id="fieldArea_postAnnouncements_rooms" class="group" id="roomGroup">
        		<legend id="fieldLabelArea_postAnnouncements_rooms_legend">
					<span>Rooms</span>
				</legend>
				<div class="field-group">
	            	<select name="roomOption" id="roomOption">
	            		<option value="all" selected>All Rooms</option>
	            		<option value="subscribed">Rooms subscribed to atleast one notification</option>
	            		<option value="specifc">Selected Rooms</option>
	            	</select>
	            </div>
				<div class="field-group" id="buttonGroup">
	            	<input type="button" id="selectAll" name="selectAll" value="Select All">
	            	<input type="button" id="selectNone" name="selectNone" value="Select None">
	            </div>
	            <div id="room-list">
					[#list rooms as room]
						<div class="checkbox">
							[@ww.checkbox name="rooms" fieldValue="${room}" theme="simple"/] ${room}
						</div>
					[/#list]
				</div>
			</fieldset>
        [/@ui.bambooSection]
[/@ww.form]
<script type="text/javascript">
	AJS.$("#buttonGroup").hide();
	AJS.$("#room-list").hide();
	
	AJS.$("#selectAll").click(function () {
		AJS.$("#room-list").find(':checkbox').prop('checked', true);
	});
	
	AJS.$("#selectNone").click(function () {
		AJS.$("#room-list").find(':checkbox').removeAttr('checked');
	});
	
	AJS.$("#roomOption").change(function () {
		if (AJS.$(this).val() == "specifc"){
			AJS.$("#buttonGroup").show();
			AJS.$("#room-list").show();
		} else {
			AJS.$("#buttonGroup").hide();
			AJS.$("#room-list").hide();
		}
	});
</script>
