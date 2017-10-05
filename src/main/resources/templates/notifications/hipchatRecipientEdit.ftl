<div class="field-group">
	<label>Select Hipchat Rooms</label>
	<span class="field-value">
		<div id="fieldArea_notification_rooms" class="checkbox">
			[#list rooms as room]
				[#if selectedRooms?seq_contains("${room}")]
					[@ww.checkbox name="rooms" fieldValue="${room}" theme="simple" checked="true"/] ${room}
				[#else]
					[@ww.checkbox name="rooms" fieldValue="${room}" theme="simple" /] ${room}
				[/#if]
				<br>
			[/#list]
		</div>
	</span>
</div>
<div class="field-group">
	<label>Notify Users?</label>
	<span class="field-value">
		<div id="fieldArea_notification_rooms" class="checkbox">
			[#if notifyUsers]
				<input type="checkbox" name="notifyUsers" checked>
			[#else]
				<input type="checkbox" name="notifyUsers">
			[/#if]
		</div>
	</span>
</div>
