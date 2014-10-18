/**
 * @author Yikai Gong
 */

var map;
var markers = [];
var datePicker;
var default_date_str = "01 October 2014";
function statistic_main(){
    drawMap();
    $('#fromTime').datetimepicker({
        language:  'en',
        weekStart: 1,
        autoclose: 1,
        startView: 1,
        minuteStep:10,
        minView: 0,
        maxView: 1,
        forceParse: 0
    });
    $('#toTime').datetimepicker({
        language:  'en',
        weekStart: 1,
        autoclose: 1,
        startView: 1,
        minuteStep:10,
        minView: 0,
        maxView: 1,
        forceParse: 0
    });
    datePicker = $('.form_date').datetimepicker({
        language:  'en',
        startDate: new Date("22 September 2014"),
        endDate: new Date(),
        weekStart: 1,
        todayBtn:  1,
        autoclose: 1,
        todayHighlight: 1,
        startView: 2,
        minView: 2,
        forceParse: 0
    }).on('changeDate', function(ev){
        var date = ev.date.valueOf();
        var d = new Date(date);
        d.setTime(d.getTime() + d.getTimezoneOffset()*60*1000 );
        d.setHours(0,0,0,0);
        date = Date.parse(d.toString());
        var dateEnd = date+86400000;
        getStreetsDataByDate(date, dateEnd);
        updatePage();
    }).datetimepicker('update', new Date(default_date_str));
//    document.getElementById("dateInput").value = default_date_str;
    var default_date = Date.parse(default_date_str);
    var default_dateEnd = default_date + 86399999;
    updatePage();
    getStreetsDataByDate(default_date, default_dateEnd);
}

var urlPrefix = 'http://115.146.92.196:5984/';
var db = 'street_tweets/';
var onStreetReady = false;
var onCityReady = false;
function getStreetsDataByDate(dateStart, dateEnd){
    var json = {startkey : dateStart, endkey : dateEnd};
    var dataArray = [];
    onStreetReady = false;
    onCityReady = false;
    for(var i=0;i<24;i++){
        dataArray.push({hour: i.toString()+":00"});
    }
    $.ajax({
        url: urlPrefix + db +'_design/Time/_view/Time_Coordinates',
        data: json,
        type: 'get',
        dataType: 'jsonp',
        success: function(data) {
            deleteMarkders();
            var rows = data.rows;
            var counter = [];
            for (var i in rows){
                //prepare markers for map
                var value = rows[i].value;
                var coordinates = value[0];
                var options = {
                    position: new google.maps.LatLng(coordinates[0], coordinates[1]),
                    icon: circle
                };
                var marker = new google.maps.Marker(options);
                marker.time = rows[i].key;
                addlistenerToMarker(marker, rows[i], map);
                markers.push(marker);

                //prepare dataArray for line chart
                if(counter[new Date(rows[i].key).getHours()]==undefined)
                    counter[new Date(rows[i].key).getHours()]=1;
                else
                    counter[new Date(rows[i].key).getHours()]+=1;
            }
            for(var i=0;i<24;i++){
                dataArray[i].onStreet = counter[i];
            }
            onStreetReady = true;
            drawAreaChart(dataArray);
            updateMarkers();
        },
        error : function(e){
            console.log(e);
        }
    });
    $.ajax({
        url: urlPrefix + 'melbourne_tweets/' +'_design/Keywords/_view/Time_Text',
        data:json,
        type: 'get',
        dataType: 'jsonp',
        success: function(data) {
            var rows = data.rows;
            var counter = [];
            for (var i in rows){
                if(counter[new Date(rows[i].key).getHours()]==undefined)
                    counter[new Date(rows[i].key).getHours()]=1;
                else
                    counter[new Date(rows[i].key).getHours()]+=1;
            }
            for(var i=0;i<24;i++){
                dataArray[i].onCity = counter[i];
            }
            onCityReady = true;
            drawAreaChart(dataArray);
        },
        error : function(e){
            console.log(e);
        }
    });
}

function drawAreaChart(dataArray){
    if(onCityReady==false || onStreetReady==false) return;
    console.log(dataArray);
    $('#lineChart').html('');
    var lineChart = new Morris.Line({
        element: 'lineChart',
        data: dataArray,
        xkey: 'hour',
        ykeys: ['onStreet', 'onCity'],
        lineColors: ['Blue', 'HotPink'],
        parseTime: false,
        labels: ['Tweets on main streets', 'Traffic relevant tweets']
    });
}

function drawMap(){
    var mapOptions = {
        zoom: 13,
        center: { lat: -37.814107, lng: 144.963280},
        mapTypeId: google.maps.MapTypeId.ROADMAP
    }
    map = new google.maps.Map(document.getElementById('map_canvas'), mapOptions);
}

function updateMarkers(){
    console.log($('.form_date').find("input").val());
    var timeStart = Date.parse($('.form_date').find("input").val());
    var timeEnd = timeStart + 86399999;
    for (var i = 0; i < markers.length; i++) {
        if(markers[i].time > timeStart && markers[i].time < timeEnd)
            markers[i].setMap(map);
        else
            markers[i].setMap(null);
    }
}


function isInfoWindowOpen(infoWindow){
    var map = infoWindow.getMap();
    return (map !== null && typeof map !== "undefined");
}

function addlistenerToMarker(marker, row, map){
    google.maps.event.addListener(marker, 'click', function(){
        $.ajax({
            url: urlPrefix + db + row.id,
            type: 'get',
            dataType: 'jsonp',
            success: function(data) {
                var infowindow = new google.maps.InfoWindow({content:data.text, title:"hi"});
                if(!isInfoWindowOpen(infowindow))
                    infowindow.open(map, marker);
                else
                    infowindow.close();
//                console.log(data.text);
            },
            error : function(e){
                console.log(e);
            }
        });

    });
}

var circle ={
    path: google.maps.SymbolPath.CIRCLE,
    fillColor: 'crimson',
    fillOpacity: 0.9,
    scale: 4,
    strokeColor: 'white',
    strokeWeight: 1
};

function updatePage(){
    var date = $('.form_date').find("input").val();
    $(".panel-label").html(date);
    updateTimePicker(date);
}

function deleteMarkders(){
    for (var i = 0; i < markers.length; i++) {
        markers[i].setMap(null);
    }
    markers = [];
}

function updateTimePicker(fromTimeStr){
    var toTimeStr = (new Date(Date.parse(fromTimeStr) + 86400000)).toString();
    $('#fromTime').datetimepicker('update', new Date(fromTimeStr));
    $('#fromTime').datetimepicker('setStartDate', new Date(fromTimeStr));
    $('#fromTime').datetimepicker('setEndDate', new Date(toTimeStr));

    $('#toTime').datetimepicker('update', new Date(toTimeStr));
    $('#toTime').datetimepicker('setStartDate', new Date(fromTimeStr));
    $('#toTime').datetimepicker('setEndDate', new Date(toTimeStr));
}