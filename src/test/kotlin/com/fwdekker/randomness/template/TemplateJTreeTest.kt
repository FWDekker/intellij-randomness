package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.Settings
import com.fwdekker.randomness.setAll
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.Tags
import com.fwdekker.randomness.testhelpers.afterNonContainer
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.getActionButton
import com.fwdekker.randomness.testhelpers.guiGet
import com.fwdekker.randomness.testhelpers.guiRun
import com.fwdekker.randomness.testhelpers.matchBundle
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.Row3
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.shouldContain
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
 * Unit tests for [TemplateJTree].
 */
@Suppress("detekt:LargeClass") // Would be weird to split up given that CUT is not split up either
object TemplateJTreeTest : FunSpec({
    tags(Tags.IDEA_FIXTURE, Tags.SWING)


    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var originalList: TemplateList
    lateinit var currentList: TemplateList
    lateinit var tree: TemplateJTree

    fun List<Scheme>.names() = this.map { it.name }


    beforeSpec {
        FailOnThreadViolationRepaintManager.install()
    }

    afterSpec {
        FailOnThreadViolationRepaintManager.uninstall()
    }

    beforeNonContainer {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        originalList =
            TemplateList(
                mutableListOf(
                    Template("Template0", mutableListOf(DummyScheme("Scheme0"), DummyScheme("Scheme1"))),
                    Template("Template1", mutableListOf(DummyScheme("Scheme2"))),
                    Template("Template2", mutableListOf(DummyScheme("Scheme3"), DummyScheme("Scheme4"))),
                )
            )
        originalList.applyContext(Settings(templateList = originalList))

        currentList = originalList.deepCopy(retainUuid = true)
        currentList.applyContext(Settings(templateList = currentList))

        tree = guiGet { TemplateJTree(originalList, currentList) }
        frame = showInFrame(tree)
    }

    afterNonContainer {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    context("selectedNodeNotRoot") {
        test("returns null if nothing is selected") {
            guiRun { tree.clearSelection() }

            tree.selectedNodeNotRoot should beNull()
        }

        test("returns the selected node otherwise") {
            val node = StateNode(currentList.templates[0])

            guiRun { tree.selectionRows = intArrayOf(0) }

            tree.selectedNodeNotRoot shouldBe node
        }
    }

    context("selectedScheme") {
        context("get") {
            test("returns null if nothing is selected") {
                guiRun { tree.clearSelection() }

                tree.selectedScheme should beNull()
            }

            test("returns the selected scheme otherwise") {
                val template = currentList.templates[0]

                guiRun { tree.selectionPath = tree.myModel.getPathToRoot(StateNode(template)) }

                tree.selectedScheme shouldBe template
            }
        }

        context("set") {
            test("removes the selection if null is set") {
                guiRun { tree.selectedScheme = null }

                tree.selectionCount shouldBe 0
            }

            test("removes the selection if the scheme cannot be found") {
                guiRun { tree.selectedScheme = DummyScheme("Not In Tree") }

                tree.selectionCount shouldBe 0
            }

            test("selects the given scheme otherwise") {
                val scheme = currentList.templates[1].schemes[0]

                guiRun {
                    tree.expandRow(1)
                    tree.selectedScheme = scheme
                }

                tree.selectionRows!! shouldContainExactly arrayOf(2)
            }
        }

        test("selects the scheme by its UUID") {
            val copy = currentList.templates[0].schemes[1].deepCopy(retainUuid = true)
            (copy as DummyScheme).name = "New Name"

            guiRun {
                tree.expandRow(0)
                tree.selectedScheme = copy
            }

            guiGet { tree.selectedScheme?.uuid } shouldBe copy.uuid
            guiGet { tree.selectedScheme } shouldNotBe copy
        }
    }

    context("selectedTemplate") {
        context("get") {
            test("returns null if nothing is selected") {
                guiRun { tree.clearSelection() }

                tree.selectedTemplate should beNull()
            }

            test("returns the parent template if a non-template scheme is selected") {
                guiRun {
                    tree.expandRow(2)
                    tree.selectionRows = intArrayOf(3)
                }

                tree.selectedTemplate shouldBe currentList.templates[2]
            }

            test("returns the selected template otherwise") {
                guiRun { tree.selectionRows = intArrayOf(1) }

                tree.selectedTemplate shouldBe currentList.templates[1]
            }
        }

        context("set") {
            test("removes the selection if null is set") {
                guiRun { tree.selectedTemplate = null }

                tree.selectionCount shouldBe 0
            }

            test("removes the selection if the template cannot be found") {
                guiRun { tree.selectedTemplate = Template("New Template") }

                tree.selectionCount shouldBe 0
            }

            test("selects the given template if it exists") {
                guiRun { tree.selectedTemplate = currentList.templates[2] }

                tree.selectionRows!! shouldContainExactly arrayOf(2)
            }
        }
    }


    context("reload") {
        test("synchronizes removed nodes") {
            guiRun { currentList.templates.remove(currentList.templates[2]) }
            guiGet { tree.rowCount } shouldBe 3

            guiRun { tree.reload() }

            guiGet { tree.rowCount } shouldBe 2
        }

        test("synchronizes added nodes") {
            guiRun { currentList.templates += Template("New Template", mutableListOf(DummyScheme("New Scheme"))) }
            guiGet { tree.rowCount } shouldBe 3

            guiRun { tree.reload() }

            guiGet { tree.rowCount } shouldBe 5 // Including new scheme
        }

        test("retains the scheme selection") {
            guiRun {
                tree.expandRow(0)
                tree.selectedScheme = currentList.templates[1].schemes[0]
            }

            guiRun { tree.reload() }

            tree.selectedScheme shouldBe currentList.templates[1].schemes[0]
        }

        test("resets the selected scheme's state to the original") {
            guiRun {
                tree.expandRow(0)
                tree.selectedScheme = currentList.templates[1].schemes[0]
            }

            guiRun { tree.reload() }

            tree.selectedScheme shouldBe originalList.templates[1].schemes[0]
        }

        test("selects the first template if the selected node was removed") {
            guiRun {
                tree.expandRow(1)
                tree.selectedScheme = currentList.templates[1].schemes[0]
            }

            guiRun {
                currentList.templates[1].schemes.clear()
                tree.reload()
            }

            tree.selectedScheme shouldBe currentList.templates[0]
        }

        test("selects the first template if no node was selected before") {
            guiRun { tree.clearSelection() }

            guiRun { tree.reload() }

            tree.selectedScheme shouldBe currentList.templates[0]
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

        test("expands new templates") {
            val template = Template("New Template", mutableListOf(DummyScheme("New Scheme")))
            guiRun { currentList.templates += template }

            guiRun { tree.reload() }

            guiGet { (tree.getPathForRow(3).lastPathComponent as StateNode).state } shouldBe template
            guiGet { tree.isExpanded(3) } shouldBe true
        }

        test(
            "keeps collapsed nodes collapsed even if that template's original row index is larger than the new total " +
                "row count"
        ) {
            guiRun {
                currentList.templates.also { it.setAll(it.takeLast(1)) }
                currentList.templates.single().schemes.also { it.setAll(it.take(1)) }
            }

            guiGet { tree.reload() }

            guiGet { tree.isCollapsed(0) } shouldBe true
        }
    }

    context("expandAll") {
        test("expands all templates") {
            guiGet { tree.rowCount } shouldBe 3

            guiRun { tree.expandAll() }

            guiGet { tree.rowCount } shouldBe 8
        }
    }


    context("addScheme") {
        context("unique name") {
            test("appends (1) if a template with the given name already exists") {
                guiRun { tree.addScheme(Template("Template0")) }

                currentList.templates[1].name shouldBe "Template0 (1)"
            }

            test("appends (2) if two templates with the given name already exist") {
                guiRun {
                    tree.addScheme(Template("Template0"))
                    tree.addScheme(Template("Template0"))
                }

                currentList.templates[2].name shouldBe "Template0 (2)"
            }

            test("appends (2) if a template ending with (1) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (1)"))
                    tree.addScheme(Template("Template0"))
                }

                currentList.templates[2].name shouldBe "Template0 (2)"
            }

            test("replaces (1) with (2) if a template ending with (1) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (1)"))
                    tree.addScheme(Template("Template0 (1)"))
                }

                currentList.templates[2].name shouldBe "Template0 (2)"
            }

            test("replaces (13) with (14) if a template ending with (13) already exists") {
                guiRun {
                    tree.addScheme(Template("Template0 (13)"))
                    tree.addScheme(Template("Template0 (13)"))
                }

                currentList.templates[2].name shouldBe "Template0 (14)"
            }
        }

        context("template") {
            test("inserts the template at the bottom if nothing is selected") {
                guiRun { tree.clearSelection() }

                guiRun { tree.addScheme(Template("New Template")) }

                currentList.templates.last().name shouldBe "New Template"
            }

            test("inserts the template below the selected template") {
                guiRun { tree.selectedScheme = currentList.templates[1] }

                guiRun { tree.addScheme(Template("New Template")) }

                currentList.templates[2].name shouldBe "New Template"
            }

            test("inserts the template below the selected scheme") {
                guiRun {
                    tree.expandRow(0)
                    tree.selectedScheme = currentList.templates[0].schemes[1]
                }

                guiRun { tree.addScheme(Template("New Template")) }

                currentList.templates[1].name shouldBe "New Template"
            }

            test("selects the inserted template") {
                guiRun { tree.selectedScheme = currentList.templates[1] }

                guiRun { tree.addScheme(Template("New Template")) }

                currentList.templates[2].name shouldBe "New Template"
            }

            test("expands the inserted template") {
                val template = Template("New Template", mutableListOf(DummyScheme("New Scheme")))
                guiRun { tree.clearSelection() }

                guiRun { tree.addScheme(template) }

                guiGet { (tree.getPathForRow(3).lastPathComponent as StateNode).state } shouldBe template
                guiGet { tree.isExpanded(3) } shouldBe true
            }
        }

        context("scheme") {
            test("fails if a scheme is inserted while nothing is selected") {
                guiRun { tree.clearSelection() }

                shouldThrow<IllegalArgumentException> { tree.addScheme(DummyScheme()) }
                    .message should matchBundle("template_list.error.wrong_child_type")
            }

            test("inserts the scheme at the bottom of the selected template") {
                guiRun { tree.selectedScheme = currentList.templates[1] }

                guiRun { tree.addScheme(DummyScheme("New Scheme")) }

                currentList.templates[1].schemes.last().name shouldBe "New Scheme"
            }

            test("inserts the scheme below the selected scheme") {
                guiRun {
                    tree.expandRow(2)
                    tree.selectedScheme = currentList.templates[2].schemes[0]
                }

                guiRun { tree.addScheme(DummyScheme("New Scheme")) }

                currentList.templates[2].schemes[1].name shouldBe "New Scheme"
            }

            test("selects the inserted scheme") {
                guiRun {
                    tree.expandRow(1)
                    tree.selectedScheme = currentList.templates[1].schemes[0]
                }

                guiRun { tree.addScheme(DummyScheme()) }

                tree.selectedScheme shouldBe currentList.templates[1].schemes[1]
            }

            test("keeps the parent expanded") {
                guiRun { tree.selectedScheme = currentList.templates[1] }

                guiRun { tree.addScheme(DummyScheme()) }

                guiGet { tree.isExpanded(1) } shouldBe true
            }

            test("expands the parent if it is not currently expanded") {
                guiRun {
                    tree.removeScheme(currentList.templates[1].schemes[0])
                    tree.selectedScheme = currentList.templates[1]
                }

                guiRun { tree.addScheme(DummyScheme()) }

                guiGet { tree.isExpanded(1) } shouldBe true
            }
        }
    }

    context("removeScheme") {
        test("removes the given node from the tree") {
            guiRun { tree.removeScheme(currentList.templates[1]) }

            currentList.templates.names() shouldContainExactly listOf("Template0", "Template2")
        }

        test("removes the selection if there is nothing to select") {
            guiRun { repeat(3) { tree.removeScheme(currentList.templates[0]) } }

            tree.selectedScheme should beNull()
        }

        test("selects the parent if the removed node had no other siblings") {
            guiRun { tree.expandRow(1) }

            guiRun { tree.removeScheme(currentList.templates[1].schemes[0]) }

            tree.selectedScheme?.name shouldBe "Template1"
        }

        test("selects the next sibling if the removed node has siblings") {
            guiRun { tree.expandRow(0) }

            guiRun { tree.removeScheme(currentList.templates[0].schemes[0]) }

            tree.selectedScheme?.name shouldBe "Scheme1"
        }

        test("keeps the parent expanded if it still has children") {
            guiRun { tree.expandRow(0) }

            guiRun { tree.removeScheme(currentList.templates[0].schemes[0]) }

            guiGet { tree.isExpanded(0) } shouldBe true
        }
    }

    context("replaceScheme") {
        test("removes the scheme if replaced with `null`") {
            val scheme = currentList.templates[1]

            guiRun { tree.replaceScheme(scheme, null) }

            currentList.templates shouldNotContain scheme
        }

        test("replaces the node with another node") {
            val oldScheme = currentList.templates[2].schemes[1]
            val newScheme = DummyScheme()

            guiRun { tree.replaceScheme(oldScheme, newScheme) }

            currentList.templates[2].schemes shouldNotContain oldScheme
            currentList.templates[2].schemes shouldContain newScheme
        }

        test("retains the current selection") {
            guiRun { tree.selectedScheme = currentList.templates[2] }

            guiRun { tree.replaceScheme(currentList.templates[1].schemes[0], DummyScheme()) }

            guiGet { tree.selectedScheme } shouldBe currentList.templates[2]
        }

        test("keeps the replaced template collapsed") {
            val oldTemplate = currentList.templates[0]
            val newTemplate = Template(schemes = mutableListOf(DummyScheme())).also { it.uuid = oldTemplate.uuid }
            guiRun { tree.collapseRow(0) }

            guiRun { tree.replaceScheme(oldTemplate, newTemplate) }

            guiGet { tree.isCollapsed(0) } shouldBe true
        }

        test("keeps the replaced template expanded") {
            val oldTemplate = currentList.templates[0]
            val newTemplate = Template(schemes = mutableListOf(DummyScheme())).also { it.uuid = oldTemplate.uuid }
            guiRun { tree.expandRow(0) }

            guiRun { tree.replaceScheme(oldTemplate, newTemplate) }

            guiGet { tree.isExpanded(0) } shouldBe true
        }

        test("keeps the parent expanded") {
            guiRun { tree.expandRow(0) }

            guiRun { tree.replaceScheme(currentList.templates[0].schemes[1], DummyScheme()) }

            guiGet { tree.isExpanded(0) } shouldBe true
        }
    }

    context("moveSchemeByOnePosition") {
        context("template") {
            test("moves a template") {
                guiRun { tree.moveSchemeByOnePosition(currentList.templates[1], moveDown = true) }

                currentList.templates.names() shouldContainExactly listOf("Template0", "Template2", "Template1")
            }

            test("selects the moved template") {
                val template = currentList.templates[2]

                guiRun { tree.selectedTemplate = template }
                guiRun { tree.moveSchemeByOnePosition(template, moveDown = false) }

                tree.selectedTemplate shouldBe template
            }

            test("keeps the moved template expanded") {
                val template = currentList.templates[2]
                guiRun { tree.expandRow(2) }

                guiRun {
                    tree.selectedTemplate = template
                    tree.moveSchemeByOnePosition(template, moveDown = false)
                }

                guiGet { tree.isExpanded(1) } shouldBe true
            }

            test("keeps the moved template collapsed") {
                guiRun { tree.collapseRow(2) }

                guiRun {
                    tree.selectedTemplate = currentList.templates[2]
                    tree.moveSchemeByOnePosition(currentList.templates[2], moveDown = false)
                }

                guiGet { tree.isCollapsed(1) } shouldBe true
            }
        }

        context("scheme") {
            test("moves a scheme") {
                guiRun { tree.expandRow(0) }

                guiRun { tree.moveSchemeByOnePosition(currentList.templates[0].schemes[1], moveDown = false) }

                currentList.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }

            test("retains the selection after moving a scheme") {
                val scheme = currentList.templates[2].schemes[0]
                guiRun {
                    tree.expandRow(2)
                    tree.selectedScheme = scheme
                }

                guiRun { tree.moveSchemeByOnePosition(scheme, moveDown = true) }

                tree.selectedScheme shouldBe scheme
            }

            test("keeps the scheme's parent expanded if the parent does not change") {
                guiRun { tree.expandRow(0) }

                guiRun { tree.moveSchemeByOnePosition(currentList.templates[0].schemes[1], moveDown = false) }

                guiGet { tree.isExpanded(0) } shouldBe true
            }

            test("if the parent changes, keeps the old parent expanded and expands the new parent") {
                guiRun {
                    tree.collapseRow(1)
                    tree.expandRow(0)
                }

                val scheme = currentList.templates[0].schemes[1]
                guiRun { tree.moveSchemeByOnePosition(scheme, moveDown = true) }

                guiGet { tree.isExpanded(0) } shouldBe true
                tree.myModel.root.descendants()[2].children shouldContain StateNode(scheme)
                guiGet { tree.isExpanded(2) } shouldBe true
            }
        }
    }

    context("canMoveSchemeByOnePosition") {
        @Suppress("BooleanLiteralArgument") // Argument names are clear from lambda later on
        withData(
            mapOf<String, Row3<() -> Scheme, Boolean, Boolean>>(
                "first template cannot move up" to row({ currentList.templates[0] }, false, false),
                "first scheme cannot move up" to row({ currentList.templates[0].schemes[0] }, false, false),
                "last template cannot move down" to row({ currentList.templates[2] }, true, false),
                "last scheme cannot move down" to row({ currentList.templates[2].schemes[1] }, true, false),
                "template can move within list" to row({ currentList.templates[1] }, false, true),
                "second-to-last template can move down" to row({ currentList.templates[1] }, true, true),
                "scheme can move within template" to row({ currentList.templates[0].schemes[1] }, false, true),
                "scheme can go to previous template" to row({ currentList.templates[2].schemes[0] }, false, true),
                "scheme can go to next template" to row({ currentList.templates[1].schemes[0] }, true, true),
            )
        ) { (scheme, moveDown, expected) ->
            tree.canMoveSchemeByOnePosition(scheme(), moveDown) shouldBe expected
        }


        test("scheme can move up into empty template") {
            guiRun {
                currentList.templates.add(index = 2, Template("Template2.5"))
                tree.reload()
            }

            tree.canMoveSchemeByOnePosition(currentList.templates[3].schemes[0], moveDown = false) shouldBe true
        }

        test("scheme can move down into empty template") {
            guiRun {
                currentList.templates.add(Template("Template3"))
                tree.reload()
            }

            tree.canMoveSchemeByOnePosition(currentList.templates[2].schemes[1], moveDown = true) shouldBe true
        }
    }


    context("buttons") {
        beforeNonContainer {
            frame.cleanUp()
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
                guiRun {
                    tree.expandRow(0)
                    tree.selectedScheme = currentList.templates[0].schemes[1]
                }

                guiRun { frame.getActionButton("Remove").click() }

                currentList.templates[0].schemes.names() shouldContainExactly listOf("Scheme0")
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
                guiRun {
                    tree.expandRow(1)
                    tree.selectedScheme = currentList.templates[1].schemes[0]
                }

                guiRun { frame.getActionButton("Copy").click() }

                currentList.templates[1].schemes.names() shouldContainExactly listOf("Scheme2", "Scheme2")
            }

            test("creates an independent copy of the selected scheme") {
                guiRun {
                    tree.expandRow(2)
                    tree.selectedScheme = currentList.templates[2].schemes[1]
                }

                guiRun { frame.getActionButton("Copy").click() }
                (currentList.templates[2].schemes[2] as DummyScheme).name = "New Name"

                currentList.templates[2].schemes.names() shouldContainExactly listOf("Scheme3", "Scheme4", "New Name")
            }

            test("ensures the copy uses the same settings state") {
                // Arrange
                val referencedTemplate = currentList.templates[0]
                val referencingScheme = TemplateReference(referencedTemplate.uuid)
                val referencingTemplate = Template(schemes = mutableListOf(referencingScheme))
                    .also { it.applyContext(currentList.context) }

                currentList.templates += referencingTemplate

                guiRun {
                    tree.reload()
                    tree.expandRow(3)
                }
                currentList.templates[3] shouldBe referencingTemplate

                // Act
                guiRun {
                    tree.selectedScheme = currentList.templates[3].schemes[0]
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
                    tree.selectedScheme = currentList.templates[0]
                    updateButtons()
                }

                frame.getActionButton("Up").isEnabled shouldBe false
            }

            test("is enabled if the selected scheme can be moved up") {
                guiRun {
                    tree.selectedScheme = currentList.templates[1]
                    updateButtons()
                }

                frame.getActionButton("Up").isEnabled shouldBe true
            }

            test("moves the selected scheme up") {
                guiRun {
                    tree.expandRow(0)
                    tree.selectedScheme = currentList.templates[0].schemes[1]
                }

                guiRun { frame.getActionButton("Up").click() }

                currentList.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
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
                    tree.selectedScheme = currentList.templates[2]
                    updateButtons()
                }

                frame.getActionButton("Down").isEnabled shouldBe false
            }

            test("is enabled if the selected scheme can be moved down") {
                guiRun {
                    tree.selectedScheme = currentList.templates[1]
                    updateButtons()
                }

                frame.getActionButton("Down").isEnabled shouldBe true
            }

            test("moves the selected scheme down") {
                guiRun {
                    tree.expandRow(2)
                    tree.selectedScheme = currentList.templates[2].schemes[0]
                }

                guiRun { frame.getActionButton("Down").click() }

                currentList.templates[2].schemes.names() shouldContainExactly listOf("Scheme4", "Scheme3")
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
                    tree.selectedScheme = currentList.templates[0]
                    updateButtons()
                }

                frame.getActionButton("Reset").isEnabled shouldBe false
            }

            test("removes the scheme if it was newly added") {
                val template = Template("New Template")
                currentList.templates += template
                guiRun { tree.reload() }

                guiRun {
                    tree.selectedScheme = template
                    frame.getActionButton("Reset").click()
                }

                currentList.templates.names() shouldNotContain "New Template"
            }

            test("resets changes to the initially selected scheme") {
                (currentList.templates[0].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[0].schemes[0].name shouldBe "New Name"

                guiRun { frame.getActionButton("Reset").click() }

                currentList.templates[0].schemes[0].name shouldBe "Scheme0"
            }

            test("resets changes to a template") {
                currentList.templates[0].name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[0].name shouldBe "New Name"

                guiRun {
                    tree.selectedScheme = currentList.templates[0]
                    frame.getActionButton("Reset").click()
                }

                currentList.templates[0].name shouldBe "Template0"
            }

            test("resets changes to the selected scheme") {
                (currentList.templates[1].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[1].schemes[0].name shouldBe "New Name"

                guiRun {
                    tree.selectedScheme = currentList.templates[1].schemes[0]
                    frame.getActionButton("Reset").click()
                }

                currentList.templates[1].schemes[0].name shouldBe "Scheme2"
            }

            test("resets the selected template's scheme order") {
                currentList.templates[0].schemes.setAll(currentList.templates[0].schemes.reversed())
                guiRun { tree.reload() }
                currentList.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")

                guiRun {
                    tree.selectedScheme = currentList.templates[0]
                    frame.getActionButton("Reset").click()
                }

                currentList.templates[0].schemes.names() shouldContainExactly listOf("Scheme0", "Scheme1")
            }

            test("resets the selected template's schemes") {
                (currentList.templates[2].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[2].schemes[0].name shouldBe "New Name"

                guiRun {
                    tree.selectedScheme = currentList.templates[2]
                    frame.getActionButton("Reset").click()
                }

                currentList.templates[2].schemes[0].name shouldBe "Scheme3"
            }

            test("resets changes multiple times") {
                (currentList.templates[0].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[0].schemes[0].name shouldBe "New Name"
                guiRun { frame.getActionButton("Reset").click() }
                (currentList.templates[0].schemes[0] as DummyScheme).name = "New Name"
                guiRun { tree.reload() }
                currentList.templates[0].schemes[0].name shouldBe "New Name"

                guiRun { frame.getActionButton("Reset").click() }

                currentList.templates[0].schemes[0].name shouldBe "Scheme0"
            }
        }
    }
})
