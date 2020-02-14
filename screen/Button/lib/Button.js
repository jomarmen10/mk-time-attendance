define({
    /* This software is in the public domain under CC0 1.0 Universal plus a Grant of Patent License. */
    data: function(){return{
        isClockIn: null
    }},
    template:
        '<div id="test" class="my-account-nav btn-group-condensed">' +
            '<button v-on:click="logTime" id="time-entry-button" class="btn btn-sm navbar-btn navbar-right">' +
                '<i class="glyphicon glyphicon-time"></i>' +
            '</button>' +
        '</div>',
        methods: {
            logTime: function(){
                var vm = this;
                $.ajax({ 
                    type:'GET', 
                    url:('/apps/TimeAndAttendance/clockInTimeEntry'),
                    dataType:'json',
                    headers: { Accept: 'application/json'},
                    success: function(){
                        if(!vm.isClockIn){
                            document.getElementById("time-entry-button").style.background = "green";
                            vm.isClockIn = true
                        } else {
                            document.getElementById("time-entry-button").style.background = "dodgerblue";
                            vm.isClockIn = false
                        }
                    }
                })
            }
        },
        mounted: function(){
            var vm = this;
            $.ajax({ 
                type:'GET', 
                url:('/rest/s1/ButtonAPI/getButton'),
                dataType:'json',
                headers: { Accept: 'application/json'},
                success: function(res){
                    if(res.openTimeEntry){
                        document.getElementById("time-entry-button").style.background = "green";
                        vm.isClockIn = true
                    }
                }
            })
        }

    });
