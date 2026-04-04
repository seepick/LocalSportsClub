package seepick.localsportsclub.view.remark

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import seepick.localsportsclub.service.GlobalRemarkService
import seepick.localsportsclub.service.model.DataStorage
import seepick.localsportsclub.service.model.GlobalRemarkType
import seepick.localsportsclub.view.common.CustomDialog
import seepick.localsportsclub.view.shared.SharedModel

class GlobalRemarkViewModel(
    private val sharedModel: SharedModel,
    private val globalRemarkService: GlobalRemarkService,
    private val dataStorage: DataStorage,
) : ViewModel() {

    private val log = logger {}
    private lateinit var currentRemarkType: GlobalRemarkType
    val remarks = mutableStateListOf<RemarkViewEntity>()
    val nameSuggestions = mutableStateListOf<String>()

    fun onViewDialog(type: GlobalRemarkType) {
        log.info { "onViewDialog()" }
        currentRemarkType = type

        remarks.clear()
        remarks.addAll(globalRemarkService.selectAll(type).sortedBy { it.name.value.lowercase() })
        nameSuggestions.clear()
        nameSuggestions.addAll(
            when (type) {
                GlobalRemarkType.Category -> {
                    dataStorage.allCategories.map { it.name }
                }

                GlobalRemarkType.Activity -> emptyList()
                GlobalRemarkType.Teacher -> emptyList()
            })

        sharedModel.customDialog.value = CustomDialog(
            title = "Global ${type.dialogTitle} Remarks",
            content = {
                RemarkView(
                    height = it,
                    remarks = remarks,
                    nameSuggestions = nameSuggestions,
                    onAddNewClicked = { addNewRemark() },
                    onDelete = { deleteRemark(it) },
                )
            },
            confirmLabel = "Save",
            showDismissButton = false,
            onConfirm = ::saveCurrentRemarks,
        )
    }

    private fun addNewRemark() {
        log.debug { "addNewRemark() for $currentRemarkType" }
        remarks.add(0, RemarkViewEntity.newPrototype(RemarkViewType.ForGlobal(currentRemarkType)))
    }

    private fun saveCurrentRemarks() {
        log.debug { "saveCurrentRemarks()" }
        globalRemarkService.reset(currentRemarkType, remarks)
        // no global view storage (mutable thingy), thus requires app restart to take effect ;)
    }

    private fun deleteRemark(remark: RemarkViewEntity) {
        log.debug { "deleteRemark($remark)" }
        val deleted = remarks.remove(remark)
        require(deleted) { "Remark $remark not found for deletion!" }
    }

    private val GlobalRemarkType.dialogTitle: String
        get() = when (this) {
            GlobalRemarkType.Activity -> "Activity"
            GlobalRemarkType.Category -> "Category"
            GlobalRemarkType.Teacher -> "Teacher"
        }
}

