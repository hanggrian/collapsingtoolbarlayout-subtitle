package com.hendraanggrian.material.subtitlecollapsingtoolbarlayout.demo

typealias Preferences = android.content.SharedPreferences

const val PREFERENCE_SHOW_BUTTONS = "show_buttons"
const val PREFERENCE_IMAGE_URL = "image_url"

const val PREFERENCE_TITLE_ENABLED = "title_enabled"
const val PREFERENCE_TITLE = "title"
const val PREFERENCE_SUBTITLE = "subtitle"

const val PREFERENCE_SCRIMS_SHOWN = "scrims_shown"
const val PREFERENCE_CONTENT_SCRIM = "content_scrim"
const val PREFERENCE_STATUSBAR_SCRIM = "statusbar_scrim"

const val PREFERENCE_COLLAPSED_TITLE_TEXT_APPEARANCE = "collapsed_title_text_appeareance"
const val PREFERENCE_EXPANDED_TITLE_TEXT_APPEARANCE = "expanded_title_text_appeareance"
const val PREFERENCE_COLLAPSED_SUBTITLE_TEXT_APPEARANCE = "collapsed_subtitle_text_appeareance"
const val PREFERENCE_EXPANDED_SUBTITLE_TEXT_APPEARANCE = "expanded_subtitle_text_appeareance"

const val PREFERENCE_COLLAPSED_TITLE_TEXT_COLOR = "collapsed_title_text_color"
const val PREFERENCE_EXPANDED_TITLE_TEXT_COLOR = "expanded_title_text_color"
const val PREFERENCE_COLLAPSED_SUBTITLE_TEXT_COLOR = "collapsed_subtitle_text_color"
const val PREFERENCE_EXPANDED_SUBTITLE_TEXT_COLOR = "expanded_subtitle_text_color"

const val PREFERENCE_COLLAPSED_GRAVITY = "collapsed_gravity"
const val PREFERENCE_EXPANDED_GRAVITY = "expanded_gravity"

const val PREFERENCE_COLLAPSED_TITLE_TYPEFACE = "collapsed_title_typeface"
const val PREFERENCE_EXPANDED_TITLE_TYPEFACE = "expanded_title_typeface"
const val PREFERENCE_COLLAPSED_SUBTITLE_TYPEFACE = "collapsed_subtitle_typeface"
const val PREFERENCE_EXPANDED_SUBTITLE_TYPEFACE = "expanded_subtitle_typeface"

const val PREFERENCE_LEFT_MARGIN = "left_margin"
const val PREFERENCE_TOP_MARGIN = "top_margin"
const val PREFERENCE_RIGHT_MARGIN = "right_margin"
const val PREFERENCE_BOTTOM_MARGIN = "bottom_margin"

const val PREFERENCE_RESET = "reset"

fun Preferences.getStringNotNull(key: String): String = checkNotNull(getString(key, null))