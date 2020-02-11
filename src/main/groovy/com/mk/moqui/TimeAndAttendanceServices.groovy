package com.mk.moqui
import org.moqui.context.ExecutionContext
import org.moqui.entity.EntityValue
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.text.DecimalFormat
import java.sql.Timestamp
import java.time.LocalDate
import groovy.time.TimeDuration
import groovy.time.TimeCategory 


public class TimeAndAttendanceServices {
    static Map calculateSumOfHours(ExecutionContext ec) {
        double sumOfHours = 0
        String dayOfTheWeek = ec.context.day
        Boolean overTime = ec.context.overTime
        int arraySize = ec.context.timeEntryList.size()
        int days = ec.context.week.toInteger()

        Calendar c = GregorianCalendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar[dayOfTheWeek])

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        String startDate = "", endDate = ""

        startDate = df.format(c.getTime()) + " 00:00:00.000"
        c.add(Calendar.DATE, days)
        endDate = df.format(c.getTime()) + " 23:59:59.999"

        SimpleDateFormat dateFormatForTS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
        Date parsedStartDate = dateFormatForTS.parse(startDate)
        Timestamp timeStampStart = new java.sql.Timestamp(parsedStartDate.getTime())

        Date parsedEndDate = dateFormatForTS.parse(endDate)
        Timestamp timeStampEnd = new java.sql.Timestamp(parsedEndDate.getTime())

        for (int i =0; i < arraySize; i++) {
            EntityValue ev = ec.context.timeEntryList[i]

            if(ev.getPlainValueMap(0).fromDate > timeStampStart && ev.getPlainValueMap(0).thruDate < timeStampEnd && ev.getPlainValueMap(0).hours != null) {
                sumOfHours = sumOfHours + ev.getPlainValueMap(0).hours
            }
        }

        if(sumOfHours > 8 && overTime == true){
            sumOfHours -= 8
        } else if (sumOfHours < 8 && overTime == true) {
            sumOfHours = 0
        }

        DecimalFormat df2 = new DecimalFormat("###.##")
        String result = "sumOfHours"
        result = result.concat(days.toString())
        def totalHours = [:]
        totalHours[result] = df2.format(sumOfHours)
        return totalHours
    }

    static Map calculateTimeSheetHours(ExecutionContext ec) {

        double sumOfHours = 0
        int arraySize = ec.context.timeEntryList.size()
        def timeSheet = ec.context.timeSheet
        def timeSheetId = ec.context.timeSheet.timesheetId

        Calendar c = GregorianCalendar.getInstance()
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        String startDate = "", endDate = ""
        startDate = df.format(timeSheet.fromDate) + " 00:00:00.000"
        endDate = df.format(timeSheet.thruDate) + " 23:59:59.999"

        SimpleDateFormat dateFormatForTS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS")
        Date parsedStartDate = dateFormatForTS.parse(startDate)
        Timestamp timeStampStart = new java.sql.Timestamp(parsedStartDate.getTime())

        Date parsedEndDate = dateFormatForTS.parse(endDate)
        Timestamp timeStampEnd = new java.sql.Timestamp(parsedEndDate.getTime())

        for (int i =0; i < arraySize; i++) {
            EntityValue ev = ec.context.timeEntryList[i]

            if(ev.getPlainValueMap(0).fromDate > timeStampStart && ev.getPlainValueMap(0).thruDate < timeStampEnd && ev.getPlainValueMap(0).hours != null) {
                sumOfHours = sumOfHours + ev.getPlainValueMap(0).hours
            }
        }


        DecimalFormat df2 = new DecimalFormat("###.##")
        Map totalHours = ["sumOfHours":df2.format(sumOfHours)]
        return totalHours

    }

    static Map calculateDayAndHours(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        def dayArraySize = ec.context.timeEntryList.size()
        def timeEntry = ec.context.timeEntryList
        def newTimeEntry = []
       
        for(int i = 0; i < dayArraySize; i++){
            timeEntry[i].fromDate = df.format(timeEntry[i].fromDate)
            if(!newTimeEntry.fromDate.contains(timeEntry[i].fromDate)){
                newTimeEntry.push(timeEntry[i])
            } else {
                timeEntry[i].hours = (timeEntry[i].hours ? timeEntry[i].hours : 0)

                newTimeEntry[0].hours += timeEntry[i].hours
            }   
        }

        Map totalHours = ["DayAndHours":newTimeEntry]
        return totalHours
    }

    static Map numberOfClockin(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        def timeEntry = ec.context.timeEntryInside
        def arraySize = ec.context.timeEntryInside.size()
        def fromDate = ec.context.fromDate
        def counter = 0
        
        for(int i = 0; i < arraySize; i++) {
            timeEntry[i].fromDate = df.format(timeEntry[i].fromDate)
            if(fromDate == timeEntry[i].fromDate){
                counter += 1
            }
        }

        Map totalHours = ["numberOfClockin":counter]
        return totalHours
    }

    static Map checkForLongShift(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        def timeEntry = ec.context.timeEntryInside
        def arraySize = ec.context.timeEntryInside.size()
        def fromDate = ec.context.fromDate
        def thruDate = ec.context.thruDate
        double container = 0
        
        for(int i = 0; i < arraySize; i++) {
            def dfFromDate = df.format(timeEntry[i].fromDate)
            if(fromDate == dfFromDate){
                def intHour = timeEntry[i].hours.toInteger()
                if(timeEntry[i].hours >= 5){
                    container = timeEntry[i].hours
                }
            }
        }

        DecimalFormat df2 = new DecimalFormat("###.##")
        Map totalHours = ["checkForLongShift":df2.format(container)]
        return totalHours
    }

    static Map calculateBreakTime(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        def arraySize = ec.context.timeEntryInside.size()
        def timeEntry = ec.context.timeEntryInside
        def fromDate = ec.context.fromDate
        def container = []
        double result = 0

        Calendar c = GregorianCalendar.getInstance()

        for(int i = 0; i < arraySize; i++){
            def dfFromDate = df.format(timeEntry[i].fromDate)
            if(fromDate == dfFromDate){
                container.add(timeEntry[i])
            }  
        }

        def contSize = container.size()
        for(int i = 0; i < contSize; i++) {
            if(i != contSize-1){
                Date parseThru = sdf.parse(container[i].thruDate.toString())
                Date parseFrom = sdf.parse(container[i+1].fromDate.toString())
                long minFrom = parseFrom.getTime() / 60000
                long minThru = parseThru.getTime() / 60000
                result += minFrom - minThru
            }

        }

        DecimalFormat df2 = new DecimalFormat("###.##")
        Map totalHours = ["calculateBreakTime":df2.format(result / 60)]
        return totalHours
        
    }

    static Map calculateLongBreak(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");

        def arraySize = ec.context.timeEntryInside.size()
        def timeEntry = ec.context.timeEntryInside
        def fromDate = ec.context.fromDate
        def container = []
        double result = 0

        Calendar c = GregorianCalendar.getInstance()

        for(int i = 0; i < arraySize; i++){
            def dfFromDate = df.format(timeEntry[i].fromDate)
            if(fromDate == dfFromDate){
                container.add(timeEntry[i])
            }  
        }

        def contSize = container.size()
        for(int i = 0; i < contSize; i++) {
            if(i != contSize-1){
                Date parseThru = sdf.parse(container[i].thruDate.toString())
                Date parseFrom = sdf.parse(container[i+1].fromDate.toString())
                long minFrom = parseFrom.getTime() / 60000
                long minThru = parseThru.getTime() / 60000
                def output = minFrom - minThru
                if(output > result){
                    result = minFrom - minThru
                }
            }

        }

        DecimalFormat df2 = new DecimalFormat("###.##")
        Map totalHours = ["calculateLongBreak":df2.format(result / 60)]
        return totalHours
        
    }

    static Map dayOfTimeEntry(ExecutionContext ec) {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        def timeEntry = ec.context.timeEntryList
        def arraySize = ec.context.timeEntryList.size()
        def fromDate = ec.context.fromDate
        def newTimeEntry = []

        for(int i = 0; i < arraySize; i++){
            def dfFromDate = df.format(timeEntry[i].fromDate)
            if(fromDate == dfFromDate){
                newTimeEntry.push(timeEntry[i])
            }
        }
        Map totalHours = ["newTimeEntry":newTimeEntry]
        return totalHours
    }

}

