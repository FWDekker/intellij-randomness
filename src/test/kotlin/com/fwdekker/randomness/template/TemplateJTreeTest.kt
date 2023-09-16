package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.afterNonContainer
import com.fwdekker.randomness.beforeNonContainer
import com.fwdekker.randomness.getActionButton
import com.fwdekker.randomness.guiGet
import com.fwdekker.randomness.guiRun
import com.fwdekker.randomness.matchBundle
import com.fwdekker.randomness.setAll
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.NamedTag
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.Row3
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.fixture.Containers.showInFrame
import org.assertj.swing.fixture.FrameFixture


/**
 * GUI tests for [TemplateJTree].
 */
object TemplateJTreeTest : FunSpec({
    tags(NamedTag("IdeaFixture"), NamedTag("Swing"))


    lateinit var ideaFixture: IdeaTestFixture

    lateinit var originalSettings: Settings
    lateinit var currentSettings: Settings
    lateinit var tree: TemplateJTree

    fun List<Scheme>.names() = this.map { it.name }


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        originalSettings =
            Settings(
                TemplateList(
                    mutableListOf(
                        Template("Template0", mutableListOf(DummyScheme("Scheme0"), DummyScheme("Scheme1"))),
                        Template("Template1", mutableListOf(DummyScheme("Scheme2"))),
                        Template("Template2", mutableListOf(DummyScheme("Scheme3"), DummyScheme("Scheme4")))
                    )
                )
            )
        originalSettings.applyContext(originalSettings)
        currentSettings = originalSettings.deepCopy(retainUuid = true)

        tree = guiGet { TemplateJTree(originalSettings, currentSettings) }
    }

    afterNonContainer {
        ideaFixture.tearDown()
    }


    context("selectedNodeNotRoot") {
        test("returns null if nothing is selected") {
            guiRun { tree.clearSelection() }

            tree.selectedNodeNotRoot should beNull()
        }

        test("returns null if the root is selected") {
            guiRun { tree.selectionPath = tree.myModel.getPathToRoot(tree.myModel.root) }

            tree.selectedNodeNotRoot should beNull()
        }

        test("returns the selected node otherwise") {
            val node = StateNode(tree.myModel.list.templates[0])

            guiRun { tree.selectionPath = tree.myModel.getPathToRoot(node) }

            tree.selectedNodeNotRoot shouldBe node
        }
    }

    context("selectedScheme") {
        context("get") {
            test("returns null if nothing is selected") {
                guiRun { tree.clearSelection() }

                tree.selectedScheme should beNull()
            }

            test("returns null if the root is selected") {
                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(tree.myModel.root) }

                tree.selectedScheme should beNull()
            }

            test("returns the selected scheme otherwise") {
                val template = tree.myModel.list.templates[0]

                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(StateNode(template)) }

                tree.selectedScheme shouldBe template
            }
        }

        context("set") {
            test("selects the first template if null is set") {
                guiRun { tree.selectedScheme = null }

                tree.selectedScheme shouldBe tree.myModel.list.templates[0]
            }

            test("selects the root if null is set and there are no templates") {
                guiRun {
                    tree.myModel.list.templates.clear()
                    tree.reload()
                    tree.selectedScheme = null
                }

                tree.selectedScheme should beNull()
            }

            test("selects the first template if the scheme cannot be found") {
                guiRun { tree.selectedScheme = DummyScheme("Not In Tree") }

                tree.selectedScheme shouldBe tree.myModel.list.templates[0]
            }

            test("selects the root if the scheme cannot be found and there are no templates") {
                guiRun {
                    tree.myModel.list.templates.clear()
                    tree.reload()
                    tree.selectedScheme = DummyScheme("Not In Tree")
                }

                tree.selectedScheme should beNull()
            }

            test("selects the given scheme if it exists") {
                val scheme = tree.myModel.list.templates[1].schemes[0]

                guiRun { tree.selectedScheme = scheme }

                tree.selectedScheme shouldBe scheme
            }
        }
    }

    context("selectedTemplate") {
        context("get") {
            test("returns null if nothing is selected") {
                guiRun { tree.clearSelection() }

                tree.selectedTemplate should beNull()
            }

            test("returns null if the root is selected") {
                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(tree.myModel.root) }

                tree.selectedTemplate should beNull()
            }

            test("returns the parent template if a non-template scheme is selected") {
                val scheme = tree.myModel.list.templates[2].schemes[0]

                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(StateNode(scheme)) }

                tree.selectedTemplate shouldBe tree.myModel.list.templates[2]
            }

            test("returns the selected template otherwise") {
                val template = tree.myModel.list.templates[0]

                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(StateNode(template)) }

                tree.selectedTemplate shouldBe template
            }
        }

        context("set") {
            test("selects the first template if null is set") {
                guiRun { tree.selectedTemplate = null }

                tree.selectedTemplate shouldBe tree.myModel.list.templates[0]
            }

            test("selects the root if null is set and there are no templates") {
                guiRun {
                    tree.myModel.list.templates.clear()
                    tree.reload()
                    tree.selectedTemplate = null
                }

                tree.selectedTemplate should beNull()
            }

            test("selects the given template if it exists") {
                val template = tree.myModel.list.templates[1]

                guiRun { tree.selectedTemplate = template }

                tree.selectedTemplate shouldBe template
            }
        }
    }


    context("reload") {
        test("synchronizes removed nodes") {
            guiRun { tree.myModel.list.templates.removeAll(tree.myModel.list.templates.take(2)) }
            guiGet { tree.rowCount } shouldBe 8

            guiRun { tree.reload() }

            guiGet { tree.rowCount } shouldBe 3
        }

        test("synchronizes added nodes") {
            guiRun { tree.myModel.list.templates += Template("New Template", mutableListOf(DummyScheme("New Scheme"))) }
            guiGet { tree.rowCount } shouldBe 8

            guiRun { tree.reload() }

            guiGet { tree.rowCount } shouldBe 10
        }

        test("retains the scheme selection") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[1].schemes[0] }

            guiRun { tree.reload() }

            tree.selectedScheme shouldBe tree.myModel.list.templates[1].schemes[0]
        }

        test("selects the first template if the selected node was removed") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[1].schemes[0] }

            guiRun {
                tree.myModel.list.templates[1].schemes.clear()
                tree.reload()
            }

            tree.selectedScheme shouldBe tree.myModel.list.templates[0]
        }

        test("selects the first template if no node was selected before") {
            guiRun { tree.clearSelection() }

            guiRun { tree.reload() }

            tree.selectedScheme shouldBe tree.myModel.list.templates[0]
        }

        test("keeps collapsed nodes collapsed") {
            guiRun { tree.collapseRow(3) }
            guiGet { tree.isCollapsed(3) } shouldBe true

            guiRun { tree.reload() }

            guiGet { tree.isCollapsed(3) } shouldBe true
        }

        test("keeps expanded nodes expanded") {
            guiRun { tree.expandRow(0) }
            guiGet { tree.isExpanded(0) } shouldBe true

            guiRun { tree.reload() }

            guiGet { tree.isExpanded(0) } shouldBe true
        }

        test("expands new nodes") {
            val template = Template("New Template", mutableListOf(DummyScheme("New Scheme")))
            guiRun { tree.myModel.list.templates += template }

            guiRun { tree.reload() }

            guiGet { tree.myModel.rowToNode(8)!!.state } shouldBe template
            guiGet { tree.isExpanded(8) } shouldBe true
        }
    }


    context("addScheme") {
        context("unique name") {
            test("appends (1) if a scheme with the given name already exists") {
                guiRun { tree.addScheme(Template("Template0")) }

                tree.myModel.list.templates[1].name shouldBe "Template0 (1)"
            }

            test("appends (2) if two schemes with the given name already exist") {
                guiRun {
                    tree.addScheme(Template("Template0"))
                    tree.addScheme(Template("Template0"))
                }

                tree.myModel.list.templates[2].name shouldBe "Template0 (2)"
            }

            test("appends (2) if a scheme ending with (1) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (1)"))
                    tree.addScheme(Template("Template0"))
                }

                tree.myModel.list.templates[2].name shouldBe "Template0 (2)"
            }

            test("replaces (1) with (2) if a scheme ending with (1) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (1)"))
                    tree.addScheme(Template("Template0 (1)"))
                }

                tree.myModel.list.templates[2].name shouldBe "Template0 (2)"
            }

            test("replaces (13) with (14) if a scheme ending with (13) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (13)"))
                    tree.addScheme(Template("Template0 (13)"))
                }

                tree.myModel.list.templates[2].name shouldBe "Template0 (14)"
            }
        }

        test("inserts the template at the bottom if nothing is selected") {
            guiRun { tree.clearSelection() }

            guiRun { tree.addScheme(Template("New Template")) }

            tree.myModel.list.templates.last().name shouldBe "New Template"
        }

        test("fails if a scheme is inserted while nothing is selected") {
            guiRun { tree.clearSelection() }

            shouldThrow<IllegalArgumentException> { tree.addScheme(DummyScheme()) }
                .message should matchBundle("template_list.error.add_template_to_non_root")
        }

        test("inserts the template below the selected template") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[1] }

            guiRun { tree.addScheme(Template("New Template")) }

            tree.myModel.list.templates[2].name shouldBe "New Template"
        }

        test("inserts the scheme at the bottom of the selected template") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[1] }

            guiRun { tree.addScheme(DummyScheme("New Scheme")) }

            tree.myModel.list.templates[1].schemes.last().name shouldBe "New Scheme"
        }

        test("inserts the template after the selected scheme") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[0].schemes[1] }

            guiRun { tree.addScheme(Template("New Template")) }

            tree.myModel.list.templates[1].name shouldBe "New Template"
        }

        test("inserts the scheme below the selected scheme") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[2].schemes[0] }

            guiRun { tree.addScheme(DummyScheme("New Scheme")) }

            tree.myModel.list.templates[2].schemes[1].name shouldBe "New Scheme"
        }

        test("selects the inserted node") {
            guiRun { tree.selectedScheme = tree.myModel.list.templates[1].schemes[0] }

            guiRun { tree.addScheme(DummyScheme()) }

            tree.selectedScheme shouldBe tree.myModel.list.templates[1].schemes[1]
        }
    }

    context("removeScheme") {
        test("removes the given node from the tree") {
            guiRun { tree.removeScheme(tree.myModel.list.templates[1]) }

            tree.myModel.list.templates.names() shouldContainExactly listOf("Template0", "Template2")
        }

        test("removes the selection if there is nothing to select") {
            guiRun { repeat(3) { tree.removeScheme(tree.myModel.list.templates[0]) } }

            tree.selectedScheme should beNull()
        }

        test("selects the parent if the removed node had no siblings left") {
            guiRun { tree.removeScheme(tree.myModel.list.templates[1].schemes[0]) }

            tree.selectedScheme?.name shouldBe "Template1"
        }

        test("selects the next sibling if the removed node has siblings") {
            guiRun { tree.removeScheme(tree.myModel.list.templates[0].schemes[0]) }

            tree.selectedScheme?.name shouldBe "Scheme1"
        }
    }

    context("moveSchemeByOnePosition") {
        test("moves a template") {
            guiRun { tree.moveSchemeByOnePosition(tree.myModel.list.templates[1], moveDown = true) }

            tree.myModel.list.templates.names() shouldContainExactly listOf("Template0", "Template2", "Template1")
        }

        test("moves a scheme") {
            guiRun { tree.moveSchemeByOnePosition(tree.myModel.list.templates[0].schemes[1], moveDown = false) }

            tree.myModel.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
        }

        test("expands and selects the moved template") {
            val template = tree.myModel.list.templates[2]

            guiRun { tree.selectedTemplate = template }
            guiRun { tree.moveSchemeByOnePosition(template, moveDown = false) }

            val row = tree.myModel.nodeToRow(StateNode(template))
            guiGet { tree.isExpanded(row) } shouldBe true // The `guiGet` is required
            tree.isRowSelected(row) shouldBe true
        }

        test("selects the moved scheme") {
            val scheme = tree.myModel.list.templates[2].schemes[0]

            guiRun { tree.moveSchemeByOnePosition(scheme, moveDown = true) }

            tree.selectedScheme?.name shouldBe scheme.name
        }
    }

    context("canMoveSchemeByOnePosition") {
        @Suppress("BooleanLiteralArgument") // Argument names clear from lambda later on
        withData(
            mapOf<String, Row3<() -> Scheme, Boolean, Boolean>>(
                "first template" to row({ tree.myModel.list.templates[0] }, false, false),
                "first scheme" to row({ tree.myModel.list.templates[0].schemes[0] }, false, false),
                "last template" to row({ tree.myModel.list.templates[2] }, true, false),
                "last scheme" to row({ tree.myModel.list.templates[2].schemes[1] }, true, false),
                "move scheme within template" to row({ tree.myModel.list.templates[0].schemes[1] }, false, true),
                "move templates within list" to row({ tree.myModel.list.templates[1] }, true, true),
                "move scheme between templates" to row({ tree.myModel.list.templates[1].schemes[0] }, true, true),
            )
        ) { (scheme, moveDown, expected) -> tree.canMoveSchemeByOnePosition(scheme(), moveDown) shouldBe expected }
    }


    context("buttons") {
        lateinit var frame: FrameFixture


        beforeNonContainer {
            frame = showInFrame(guiGet { tree.asDecoratedPanel() })
        }

        afterNonContainer {
            frame.cleanUp()
        }


        fun updateButtons() = (frame.getActionButton("Add").parent as ActionToolbar).updateActionsImmediately()


        xtest("AddButton") {
            // No non-popup behavior to test
        }

        context("RemoveButton") {
            test("is disabled if nothing is selected") {
                guiRun {
                    tree.clearSelection()
                    updateButtons()
                }

                frame.getActionButton("Remove").isEnabled shouldBe false
            }

            test("removes the selected scheme") {
                guiRun { tree.selectedScheme = tree.myModel.list.templates[0].schemes[1] }

                guiRun { frame.getActionButton("Remove").click() }

                tree.myModel.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme0")
            }
        }

        context("CopyButton") {
            test("is disabled if nothing is selected") {
                guiRun {
                    tree.clearSelection()
                    updateButtons()
                }

                frame.getActionButton("Copy").isEnabled shouldBe false
            }

            test("copies the selected scheme") {
                guiRun { tree.selectedScheme = tree.myModel.list.templates[1].schemes[0] }

                guiRun { frame.getActionButton("Copy").click() }

                tree.myModel.list.templates[1].schemes.names() shouldContainExactly listOf("Scheme2", "Scheme2")
            }

            test("creates an independent copy of the selected scheme") {
                guiRun { tree.selectedScheme = tree.myModel.list.templates[2].schemes[1] }

                guiRun { frame.getActionButton("Copy").click() }
                (tree.myModel.list.templates[2].schemes[2] as DummyScheme).name = "New Name"

                tree.myModel.list.templates[2].schemes[1].name shouldNotBe "New Name"
            }

            test("ensures the copy uses the same settings state") {
                // Arrange
                val referencedTemplate = tree.myModel.list.templates[0]
                val referencingScheme = TemplateReference(referencedTemplate.uuid)
                val referencingTemplate = Template(schemes = mutableListOf(referencingScheme))
                    .also { it.applyContext(currentSettings) }

                currentSettings.templates += referencingTemplate
                currentSettings.applyContext(currentSettings)

                guiRun { tree.reload() }
                tree.myModel.list.templates.last() shouldBe referencingTemplate

                // Act
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[3].schemes[0]
                    frame.getActionButton("Copy").click()
                }

                // Assert
                val selectedScheme = tree.selectedScheme!! as TemplateReference
                selectedScheme shouldBe referencingScheme
                selectedScheme shouldNotBeSameInstanceAs referencingScheme
                selectedScheme.template shouldBeSameInstanceAs referencedTemplate
            }
        }

        context("UpButton") {
            test("is disabled if nothing is selected") {
                guiRun {
                    tree.clearSelection()
                    updateButtons()
                }

                frame.getActionButton("Up").isEnabled shouldBe false
            }

            test("is disabled if the selected scheme cannot be moved up") {
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[0]
                    updateButtons()
                }

                frame.getActionButton("Up").isEnabled shouldBe false
            }

            test("is enabled if the selected scheme can be moved up") {
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[1]
                    updateButtons()
                }

                frame.getActionButton("Up").isEnabled shouldBe true
            }

            test("moves the selected scheme up") {
                guiRun { tree.selectedScheme = tree.myModel.list.templates[0].schemes[1] }

                guiRun { frame.getActionButton("Up").click() }

                tree.myModel.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }
        }

        context("DownButton") {
            test("is disabled if nothing is selected") {
                guiRun {
                    tree.clearSelection()
                    updateButtons()
                }

                frame.getActionButton("Down").isEnabled shouldBe false
            }

            test("is disabled if the selected scheme cannot be moved down") {
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[2]
                    updateButtons()
                }

                frame.getActionButton("Down").isEnabled shouldBe false
            }

            test("is enabled if the selected scheme can be moved down") {
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[1]
                    updateButtons()
                }

                frame.getActionButton("Down").isEnabled shouldBe true
            }

            test("moves the selected scheme down") {
                guiRun { tree.selectedScheme = tree.myModel.list.templates[2].schemes[0] }

                guiRun { frame.getActionButton("Down").click() }

                tree.myModel.list.templates[2].schemes.names() shouldContainExactly listOf("Scheme4", "Scheme3")
            }
        }

        context("ResetButton") {
            test("is disabled if nothing is selected") {
                guiRun {
                    tree.clearSelection()
                    updateButtons()
                }

                frame.getActionButton("Reset").isEnabled shouldBe false
            }

            test("is disabled if the selection has not been modified") {
                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[0]
                    updateButtons()
                }

                frame.getActionButton("Reset").isEnabled shouldBe false
            }

            test("removes the scheme if it was newly added") {
                val template = Template("New Template")
                tree.myModel.list.templates += template
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = template
                    frame.getActionButton("Reset").click()
                }

                tree.myModel.list.templates.names() shouldNotContain "New Template"
            }

            test("resets changes to the initially selected scheme") {
                (tree.myModel.list.templates[0].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }

                guiRun { frame.getActionButton("Reset").click() }

                (tree.myModel.list.templates[0].schemes[0] as DummyScheme).name shouldBe "Scheme0"
            }

            test("resets changes to a template") {
                tree.myModel.list.templates[0].name = "New Name"
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[0]
                    frame.getActionButton("Reset").click()
                }

                tree.myModel.list.templates[0].name shouldBe "Template0"
            }

            test("resets changes to a scheme") {
                (tree.myModel.list.templates[1].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[1].schemes[0]
                    frame.getActionButton("Reset").click()
                }

                tree.myModel.list.templates[1].schemes[0].name shouldBe "Scheme2"
            }

            test("resets a template's scheme order") {
                tree.myModel.list.templates[0].schemes.setAll(tree.myModel.list.templates[0].schemes.reversed())
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[0]
                    frame.getActionButton("Reset").click()
                }

                tree.myModel.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme0", "Scheme1")
            }

            test("resets a template's schemes") {
                (tree.myModel.list.templates[2].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = tree.myModel.list.templates[2]
                    frame.getActionButton("Reset").click()
                }

                tree.myModel.list.templates[2].schemes[0].name shouldBe "Scheme3"
            }
        }
    }
})
