<b>Hipchat Rooms</b>: 
[#list selectedRooms as room]
	${room}, 
[/#list]
<br>
<b>Notify Users?</b>: ${notifyUsers!?string}