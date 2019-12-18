package com.fwdekker.randomness.array

import com.intellij.application.options.schemes.AbstractSchemeActions
import com.intellij.application.options.schemes.SchemesModel
import com.intellij.application.options.schemes.SimpleSchemesPanel


class ArraySchemesPanel(private val settings: ArraySettings) :
    SimpleSchemesPanel<ArrayScheme>(), SchemesModel<ArrayScheme> {
    private var count = 0
    private fun start() = List(count++) { ">" }.joinToString("")
    private fun end() = List(--count) { ">" }.joinToString("")

    private val listeners = mutableListOf<Listener>()


    fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    fun updateComboBoxList() {
        val currentScheme = settings.currentScheme

        println("${start()} Resetting combobox; current scheme is `${settings.currentSchemeName}`")
        resetSchemes(settings.schemes)
        println("${end()} Reset combobox; current scheme name is `${settings.currentSchemeName}`")

        println("${start()} Selecting scheme `${settings.currentSchemeName}`")
        selectScheme(currentScheme)
        println("${end()} Selected scheme `${settings.currentSchemeName}`")
    }

    override fun removeScheme(scheme: ArrayScheme) {
        require(differsFromDefault(scheme)) { "Cannot remove default scheme." }
        require(settings.schemes.size > 1) { "Cannot remove only scheme." }

        println("${start()} Removing scheme `${scheme.name}`")
        if (scheme == settings.currentScheme)
            settings.currentSchemeName = ArrayScheme.DEFAULT_NAME
        settings.schemes.remove(scheme)

        updateComboBoxList()

        listeners.forEach { it.onSchemeSwitched(scheme) }
        println("${end()} Removed scheme `${scheme.name}`")
    }

    override fun containsScheme(name: String, projectScheme: Boolean) = settings.schemes.any { it.name == name }

    override fun createSchemeActions() = object : AbstractSchemeActions<ArrayScheme>(this) {
        override fun onSchemeChanged(scheme: ArrayScheme?) {
            val newSchemeName = scheme?.name ?: "NULL"
            println("${start()} Changing scheme from `${settings.currentSchemeName}` to `$newSchemeName`")

            if (scheme == null) {
                println("${end()} Changed scheme from `${settings.currentSchemeName}` to `$newSchemeName`")
                return
            }

            listeners.forEach { it.onSchemeWillSwitch(settings.currentScheme) }

            settings.currentScheme = scheme

            listeners.forEach { it.onSchemeSwitched(scheme) }
            println("${end()} Changed scheme from `${settings.currentSchemeName}` to `$newSchemeName`")
        }

        override fun renameScheme(scheme: ArrayScheme, newName: String) {
            require(differsFromDefault(scheme)) { "Cannot rename default scheme." }

            println("${start()} Renaming scheme `${scheme.name}` to `$newName`")
            listeners.forEach { it.onSchemeWillSwitch(scheme) }

            scheme.myName = newName
            settings.currentScheme = scheme
            updateComboBoxList()

            listeners.forEach { it.onSchemeSwitched(scheme) }
            println("${end()} Renamed scheme `${scheme.name}` to `$newName`")
        }

        override fun resetScheme(scheme: ArrayScheme) {
            require(scheme.name == ArrayScheme.DEFAULT_NAME) { "Cannot reset non-default scheme." }
            scheme.copyFrom(ArrayScheme(myName = scheme.myName))
        }

        override fun duplicateScheme(scheme: ArrayScheme, newName: String) {
            println("${start()} Duplicating scheme `${scheme.name}` as `$newName`")
            listeners.forEach { it.onSchemeWillSwitch(scheme) }

            val copy = scheme.copy(myName = newName)
            settings.schemes.add(copy)
            settings.currentScheme = copy

            listeners.forEach { it.onSchemeSwitched(copy) }
            println("${end()} Duplicated scheme `${scheme.name}` as `$newName`")

            updateComboBoxList()
        }

        override fun getSchemeType() = ArrayScheme::class.java
    }


    override fun getModel() = this

    override fun differsFromDefault(scheme: ArrayScheme) = scheme.name != ArrayScheme.DEFAULT_NAME

    override fun supportsProjectSchemes() = false

    override fun highlightNonDefaultSchemes() = true

    override fun useBoldForNonRemovableSchemes() = true

    override fun isProjectScheme(scheme: ArrayScheme) = false

    override fun canDeleteScheme(scheme: ArrayScheme) = differsFromDefault(scheme)

    override fun canDuplicateScheme(scheme: ArrayScheme) = true

    override fun canRenameScheme(scheme: ArrayScheme) = differsFromDefault(scheme)

    override fun canResetScheme(scheme: ArrayScheme) = !differsFromDefault(scheme)


    interface Listener {
        fun onSchemeWillSwitch(scheme: ArrayScheme)

        fun onSchemeSwitched(scheme: ArrayScheme)
    }
}
