//Set Required Variables

var message='<b>Error Contacting Server...</b>'

var backgroundcolor="yellow"
var msgwidth = "80px"
var msgheight = "26px"
var bgimage = "img/loading_bg.gif";

/////////////// DO NOT edit below this line

var ie=document.all
var ieNOTopera=document.all&&navigator.userAgent.indexOf("Opera")==-1

function regenerate(){
window.location.reload()
}

function regenerate2(){
if (document.layers)
setTimeout("window.onresize=regenerate",400)
}

if (ie||document.getElementById)
document.write('<div id="topmsg" style="position:absolute;visibility:hidden;">'+message+'</div>')

var topmsg_obj=ie? document.all.topmsg : document.getElementById? document.getElementById("topmsg") : document.topmsg

function positionit() {
var dsocleft=ie? document.body.scrollLeft : pageXOffset
var dsoctop=ie? document.body.scrollTop : pageYOffset
var window_width=ieNOTopera? document.body.clientWidth : window.innerWidth-20
var window_height=ieNOTopera? document.body.clientHeight : window.innerHeight

if (ie||document.getElementById){
var _left = parseInt(dsocleft)+window_width/2-topmsg_obj.offsetWidth/2
topmsg_obj.style.left = _left + "px"
var _top = parseInt(dsoctop)-topmsg_obj.offsetHeight+30
topmsg_obj.style.top= _top+"px"
} else if (document.layers){
topmsg_obj.left=dsocleft+window_width/2-topmsg_obj.document.width/2
topmsg_obj.top=dsoctop+window_height-topmsg_obj.document.height-5
}
}

function setmessage() {
if (document.layers){
topmsg_obj=new Layer(window.innerWidth)
topmsg_obj.bgColor=backgroundcolor
regenerate2()
topmsg_obj.document.write(message)
topmsg_obj.document.close()
positionit()
topmsg_obj.visibility="show"
}
else{
positionit()
topmsg_obj.style.backgroundColor=backgroundcolor
//topmsg_obj.style.visibility="visible"
}

setInterval("positionit()",100)

if(bgimage!="")
topmsg_obj.style.background="transparent url('" + bgimage + "') no-repeat";

topmsg_obj.style.width=msgwidth
topmsg_obj.style.height=msgheight
}

function toggle_msg() {
if(topmsg_obj.style.visibility=='hidden') {
topmsg_obj.style.visibility="visible"
} else topmsg_obj.style.visibility="hidden"
}

function showloadingmsg() {
topmsg_obj.style.visibility="visible"
}

function hideloadingmsg() {
topmsg_obj.style.visibility="hidden"
}

if (document.layers||ie||document.getElementById)
window.onload=setmessage
