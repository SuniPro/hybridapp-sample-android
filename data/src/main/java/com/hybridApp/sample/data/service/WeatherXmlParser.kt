package com.hybridApp.sample.data.service

import android.util.Xml
import com.hybridApp.sample.data.model.Weather
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream
import java.io.StringReader

class WeatherXmlParser {

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(reader: StringReader): Weather? {
        reader.use { reader ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(reader)
            parser.nextTag()

            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): Weather? {
        inputStream.use { inputStream ->
            val parser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()

            return readFeed(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readFeed(parser: XmlPullParser): Weather? {
        var result: Weather? = null

        parser.require(XmlPullParser.START_TAG, null, "rss")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "channel") {
                result = readChannel(parser, parser.name)
                break;
            }
            skip(parser)
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readChannel(parser: XmlPullParser, name: String): Weather? {
        var result: Weather? = null

        parser.require(XmlPullParser.START_TAG, null, name)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "item") {
                result = readItem(parser, parser.name)
                break
            }
            skip(parser)

        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readItem(parser: XmlPullParser, name: String): Weather? {
        var result: Weather? = null

        parser.require(XmlPullParser.START_TAG, null, name)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "description") {
                result = readDescription(parser, parser.name)
                break
            }
            skip(parser)

        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readDescription(parser: XmlPullParser, name: String): Weather? {
        var result: Weather? = null

        parser.require(XmlPullParser.START_TAG, null, name)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "body") {
                result = readBody(parser, parser.name)
                break
            }
            skip(parser)

        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readBody(parser: XmlPullParser, name: String): Weather? {
        var result: Weather? = null

        parser.require(XmlPullParser.START_TAG, null, name)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            if (parser.name == "data") {
                val seq = parser.getAttributeValue(null, "seq")
                if (seq == "0") {
                    result = readData(parser, parser.name)
                    break
                }
            }
            skip(parser)

        }
        return result
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun readData(parser: XmlPullParser, name: String): Weather {

        var temp: String = ""   // 온도
        var wfKor: String = ""  // 날씨한국어(맑음,구름 조금,구름 많음,흐림,비,눈/비,눈)
        /* 날씨영어(Clear,Partly Cloudy,Mostly Cloudy,Cloudy,Rain,Snow/Rain,Snow) */
        var wfEn: String = ""
        //var pop: String = ""     // 강수확률 %
        var ws: String = ""     // 풍속 m/s
        var wd: Int = 0         // 풍향 0~7(북,북동,동,남동,남,남서,서,북서)
        var wdKor: String = ""  // 풍향한국어
        var wdEn: String = ""   // 풍향영어
        //var reh: String = ""    // 습도 %

        parser.require(XmlPullParser.START_TAG, null, name)
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue

            when (parser.name) {
                "temp" -> temp = readTagData(parser, parser.name)
                "wfKor" -> wfKor = readTagData(parser, parser.name)
                "wfEn" -> wfEn = readTagData(parser, parser.name)
                "ws" -> ws = readTagData(parser, parser.name)
                "wd" -> wd = readTagData(parser, parser.name).toInt()
                "wdKor" -> wdKor = readTagData(parser, parser.name)
                "wdEn" -> wdEn = readTagData(parser, parser.name)
                else -> skip(parser)
            }
        }
        return Weather(temp, wfKor, wfEn, ws, wd, wdKor, wdEn)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTagData(parser: XmlPullParser, name: String): String {
        parser.require(XmlPullParser.START_TAG, null, name)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, null, name)
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        return result
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}