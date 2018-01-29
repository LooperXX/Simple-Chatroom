var $messages = $('.messages-content'),
    d, m,
    i = 0;
var k = 0;
var isdragging1 = false;
var isdragging2 = false;

$("#newBot").keydown(function (e) {
    e.preventDefault();
    if (window.event.keyCode === 13) {
        sendMsgToNewBot();
        insertMessage();
    }
});

function show() {
    var display =  $('#show').css("display");
    if(display == "none" && !isdragging1){
        $('#show').show();
        if(k == 0){
            chatStart();
        }
        k++;
    }else if (display == "block"){
        $('#show').hide();
    }
}

function minimize() {
    $('#show').hide();
    buttonshow();
}
function buttonshow() {
    $('.menu .items span').toggleClass('active');
    $('.menu .button').toggleClass('active');
}

function chatStart() {
    $messages.mCustomScrollbar();
    setTimeout(function () {
        fakeMessage();
    }, 100);
}

function updateScrollbar() {
    $messages.mCustomScrollbar("update").mCustomScrollbar('scrollTo', 'bottom', {
        scrollInertia: 10,
        timeout: 0
    });
}

function setDate() {
    d = new Date()
    m = d.getMinutes();
    $('<div class="timestamp">' + d.getHours() + ':' + m + '</div>').appendTo($('.message:last'));
    $('<div class="checkmark-sent-delivered">&check;</div>').appendTo($('.message:last'));
    $('<div class="checkmark-read">&check;</div>').appendTo($('.message:last'));
}

function insertMessage() {
    msg = $('.message-input').val();
    if ($.trim(msg) == '') {
        return false;
    }
    $('<div class="message message-personal">' + msg + '</div>').appendTo($('.mCSB_container')).addClass('new');
    setDate();
    $('.message-input').val(null);
    updateScrollbar();
    setTimeout(function () {}, 100 + (Math.random() * 20) * 100);
}

var Fake = [
    'Hi there, I\'m Violet and you?',
    'Nice to meet you',
    'How are you?',
    'Not too bad, thanks',
    'What do you do?',
    'That\'s awesome',
    'I think you\'re a nice person',
    'Why do you think that?',
    'Can you explain?',
    'Anyway I\'ve gotta go now',
    'It was a pleasure chat with you',
    'Time to deliver letters',
    'Bye',
    ':)'
];

function showMsgFromNewBot(messgae) {
    $('<div class="message loading new"></div>').appendTo($('.mCSB_container'));
    updateScrollbar();
    setTimeout(function () {
        $('.message.loading').remove();
        $('<div class="message new">' + messgae + '</div>').appendTo($('.mCSB_container')).addClass('new');
        setDate();
        updateScrollbar();
    }, 100 + (Math.random() * 20) * 100);
}

function fakeMessage() {
    if ($('.message-input').val() !== '') {
        return false;
    }
    $('<div class="message loading new"></div>').appendTo($('.mCSB_container'));
    updateScrollbar();
    setTimeout(function () {
        $('.message.loading').remove();
        $('<div class="message new">' + Fake[i] + '</div>').appendTo($('.mCSB_container')).addClass('new');
        setDate();
        updateScrollbar();
        i++;
    }, 100 + (Math.random() * 20) * 100);

}

$('.bubble').draggable();

$('#bubble1').click(function () {
    buttonshow();
});

$('#bubble1').mousedown(function(){
    isdragging1 = false;
});
$('#bubble1').mousemove(function(){
    isdragging1 = true;
    $('#bubble1').css("transition", "all 0s");
});
$('#bubble2').mousemove(function(){
    isdragging2 = true;
    $('#bubble2').css("transition", "all 0s");
});
$('#bubble1').mouseup(function(e){
    e.preventDefault();
    var lastY = window.event.clientY;
    var lastX = window.event.clientX;
    var swidth = $( window ).width();
    var height = $( window ).height();
    if(isdragging1){
        if(lastX > (swidth/2)){
            $(this).css("top", lastY).css("left", (swidth-55) + "px").css("transition", "all 0.4s");
        }else{
            $(this).css("top", lastY).css("left", "-25px").css("transition", "all 0.4s");
        }
        if(lastY < 90 ){
            $(this).css("top",height/2);
        }
        if(lastY > height-55 ){
            $(this).css("top",height/2);
        }
    }
});
$('#bubble2').mouseup(function (e) {
    e.preventDefault();
    var lastY = window.event.clientY;
    var lastX = window.event.clientX;
    var swidth = $( window ).width();
    var height = $( window ).height();
    if(lastX > (swidth/2)){
        $(this).css("top", lastY).css("left", (swidth-35) + "px").css("transition", "all 0.4s");
    }else{
        $(this).css("top", lastY).css("left", "-25px").css("transition", "all 0.4s");
    }
    if(lastY < 50 ){
        $(this).css("top",height/2);
    }
    if(lastY > height-35 ){
        $(this).css("top",height/2);
    }
})