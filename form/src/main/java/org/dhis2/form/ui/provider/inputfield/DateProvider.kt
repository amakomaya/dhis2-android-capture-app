package org.dhis2.form.ui.provider.inputfield

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import org.dhis2.form.extensions.inputState
import org.dhis2.form.extensions.legend
import org.dhis2.form.extensions.supportingText
import org.dhis2.form.model.FieldUiModel
import org.dhis2.form.ui.intent.FormIntent
import org.hisp.dhis.android.core.common.ValueType
import org.hisp.dhis.mobile.ui.designsystem.component.DateTimeActionType
import org.hisp.dhis.mobile.ui.designsystem.component.InputDateTime
import org.hisp.dhis.mobile.ui.designsystem.component.InputStyle
import org.hisp.dhis.mobile.ui.designsystem.component.SelectableDates
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.DateTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.model.TimeTransformation
import org.hisp.dhis.mobile.ui.designsystem.component.state.InputDateTimeData
import org.hisp.dhis.mobile.ui.designsystem.component.state.rememberInputDateTimeState
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

// Nepali Date Picker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePicker
import dev.shivathapaa.nepalidatepickerkmp.NepaliDatePickerState
import dev.shivathapaa.nepalidatepickerkmp.DisplayMode
import dev.shivathapaa.nepalidatepickerkmp.calendar_model.NepaliDateConverter
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDateLocale
import dev.shivathapaa.nepalidatepickerkmp.data.NepaliDatePickerLang

@Composable
fun ProvideInputDate(
    modifier: Modifier,
    inputStyle: InputStyle,
    fieldUiModel: FieldUiModel,
    intentHandler: (FormIntent) -> Unit,
    onNextClicked: () -> Unit,
) {
    val (actionType, visualTransformation) =
        when (fieldUiModel.valueType) {
            ValueType.DATETIME -> DateTimeActionType.DATE_TIME to DateTimeTransformation()
            ValueType.TIME -> DateTimeActionType.TIME to TimeTransformation()
            else -> DateTimeActionType.DATE to DateTransformation()
        }

    val textSelection = TextRange(fieldUiModel.value?.length ?: 0)
    val yearIntRange = getYearRange(fieldUiModel)
    val selectableDates = getSelectableDates(fieldUiModel)

    var value by remember(fieldUiModel.value) {
        mutableStateOf(
            fieldUiModel.value?.let {
                TextFieldValue(
                    formatStoredDateToUI(it, fieldUiModel.valueType),
                    textSelection
                )
            } ?: TextFieldValue()
        )
    }

    val inputState =
        rememberInputDateTimeState(
            InputDateTimeData(
                title = fieldUiModel.label,
                actionType = actionType,
                visualTransformation = visualTransformation,
                isRequired = fieldUiModel.mandatory,
                selectableDates = selectableDates,
                yearRange = yearIntRange,
                inputStyle = inputStyle,
            ),
            inputTextFieldValue = value,
            inputState = fieldUiModel.inputState(),
            legendData = fieldUiModel.legend(),
            supportingText = fieldUiModel.supportingText(),
        )

    // Nepali picker state
    var showNepaliPicker by remember { mutableStateOf(false) }

    val nepaliLocale = remember {
        NepaliDateLocale(
            language = NepaliDatePickerLang.NEPALI
        )
    }

    val nepaliPickerState = remember {
        NepaliDatePickerState(
            locale = nepaliLocale,
            initialDisplayMode = DisplayMode.Picker
        )
    }


    LaunchedEffect(nepaliPickerState.selectedDate) {
        val bsDate = nepaliPickerState.selectedDate ?: return@LaunchedEffect
        showNepaliPicker = false

        val adDate = NepaliDateConverter.convertNepaliToEnglish(
            bsDate.year, bsDate.month, bsDate.dayOfMonth)

        val adFormatted = "%04d-%02d-%02d".format(adDate.year, adDate.month, adDate.dayOfMonth)
        val bsFormatted = "%02d/%02d/%04d".format(bsDate.dayOfMonth, bsDate.month, bsDate.year)

        value = TextFieldValue(
            text = bsFormatted,
            selection = TextRange(bsFormatted.length)
        )

        intentHandler(
            FormIntent.OnSave(
                uid = fieldUiModel.uid,
                value = adFormatted,
                valueType = fieldUiModel.valueType,
                allowFutureDates = fieldUiModel.allowFutureDates
            )
        )
    }


    InputDateTime(
        state = inputState,
        modifier = modifier.semantics {
            contentDescription =
                formatStoredDateToUI(value.text, fieldUiModel.valueType)
        },
        onValueChanged = {
            if (fieldUiModel.valueType != ValueType.DATE) {
                value = it ?: TextFieldValue()
                val intent =
                    if (checkValueLengthWithTypeIsValid(value.text.length, fieldUiModel.valueType)) {
                        FormIntent.OnSave(
                            uid = fieldUiModel.uid,
                            value = value.text,
                            valueType = fieldUiModel.valueType,
                            allowFutureDates = fieldUiModel.allowFutureDates
                        )
                    } else {
                        FormIntent.OnTextChange(
                            uid = fieldUiModel.uid,
                            value = value.text,
                            valueType = fieldUiModel.valueType
                        )
                    }
                intentHandler(intent)
            }
        },
        onActionClicked = {
            if (fieldUiModel.valueType == ValueType.DATE) {
                showNepaliPicker = !showNepaliPicker
            }
        },
        onNextClicked = onNextClicked
    )

    if (showNepaliPicker) {
        NepaliDatePicker(
            state = nepaliPickerState,
            showTodayButton = true
        )
    }
}

fun checkValueLengthWithTypeIsValid(
    length: Int,
    valueType: ValueType?,
): Boolean =
    when (valueType) {
        ValueType.DATETIME -> length == 16
        ValueType.TIME -> length == 5
        else -> length == 10
    }

private fun getSelectableDates(uiModel: FieldUiModel): SelectableDates =
    if (uiModel.selectableDates == null) {
        if (uiModel.allowFutureDates == true) {
            SelectableDates(DEFAULT_MIN_DATE, DEFAULT_MAX_DATE)
        } else {
            SelectableDates(
                DEFAULT_MIN_DATE,
                SimpleDateFormat("ddMMyyyy", Locale.US).format(
                    Date(System.currentTimeMillis() - 1000)
                )
            )
        }
    } else {
        uiModel.selectableDates!!
    }

private fun getYearRange(uiModel: FieldUiModel): IntRange {
    val toYear =
        if (uiModel.allowFutureDates == true) 2124
        else Calendar.getInstance()[Calendar.YEAR]

    return IntRange(
        uiModel.selectableDates?.initialDate?.substring(4, 8)?.toInt() ?: 1924,
        uiModel.selectableDates?.endDate?.substring(4, 8)?.toInt() ?: toYear
    )
}

private fun formatStoredDateToUI(
    inputDateString: String,
    valueType: ValueType?,
): String {
    if (inputDateString.isBlank()) return ""

    return try {
        when (valueType) {
            ValueType.DATETIME -> {
                // Expected: yyyy-MM-ddTHH:mm
                val parts = inputDateString.split("T")
                if (parts.size != 2) return inputDateString

                val date = parts[0].split("-")
                val time = parts[1].split(":")

                if (date.size != 3 || time.size < 2) return inputDateString

                val (y, m, d) = date
                val (hh, mm) = time

                "$d$m$y$hh$mm"
            }

            ValueType.TIME -> {
                val time = inputDateString.split(":")
                if (time.size < 2) return inputDateString
                "${time[0]}${time[1]}"
            }

            else -> {
                // DATE
                val date = inputDateString.split("-")
                if (date.size != 3) return inputDateString

                val (y, m, d) = date
                "$d$m$y"
            }
        }
    } catch (e: Exception) {
        inputDateString
    }
}


const val DEFAULT_MIN_DATE = "12111924"
const val DEFAULT_MAX_DATE = "12112124"
