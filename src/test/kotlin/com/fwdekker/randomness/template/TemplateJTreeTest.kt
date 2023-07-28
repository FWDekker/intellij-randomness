package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.StateContext
import com.fwdekker.randomness.getActionButton
import com.fwdekker.randomness.string.StringScheme
import com.intellij.openapi.actionSystem.ActionToolbar
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import javax.swing.JPanel


/**
 * GUI tests for [TemplateJTree].
 */
object TemplateJTreeTest : DescribeSpec({
    lateinit var ideaFixture: IdeaTestFixture

    lateinit var originalState: StateContext
    lateinit var currentState: StateContext
    lateinit var tree: TemplateJTree

    fun model() = tree.myModel
    fun root() = tree.myModel.root
    fun list() = tree.myModel.list


    beforeContainer {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEach {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        originalState =
            StateContext(
                TemplateList(
                    listOf(
                        Template("Captain", listOf(DummyScheme.from("window"), DummyScheme.from("uncle"))),
                        Template("Particle", listOf(DummyScheme.from("republic"))),
                        Template("Village", listOf(DummyScheme.from("consider"), DummyScheme.from("tent")))
                    )
                )
            )
        originalState.templateList.applySettingsState(originalState)
        currentState = originalState.deepCopy(retainUuid = true)

        tree = GuiActionRunner.execute<TemplateJTree> { TemplateJTree(originalState, currentState) }
    }

    afterEach {
        ideaFixture.tearDown()
    }


    describe("selectedNodeNotRoot") {
        it("returns null if nothing is selected") {
            GuiActionRunner.execute { tree.clearSelection() }

            assertThat(tree.selectedNodeNotRoot).isNull()
        }

        it("returns null if the root is selected") {
            GuiActionRunner.execute { tree.selectionPath = model().getPathToRoot(root()) }

            assertThat(tree.selectedNodeNotRoot).isNull()
        }

        it("returns the selected node otherwise") {
            GuiActionRunner.execute {
                tree.selectionPath = model().getPathToRoot(StateNode(list().templates[0]))
            }

            assertThat(tree.selectedNodeNotRoot).isEqualTo(StateNode(list().templates[0]))
        }
    }

    describe("selectedScheme") {
        describe("get") {
            it("returns null if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(tree.selectedScheme).isNull()
            }

            it("returns null if the root is selected") {
                GuiActionRunner.execute { tree.selectionPath = model().getPathToRoot(root()) }

                assertThat(tree.selectedScheme).isNull()
            }

            it("returns the selected scheme otherwise") {
                GuiActionRunner.execute {
                    tree.selectionPath = model().getPathToRoot(StateNode(list().templates[0]))
                }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[0])
            }
        }

        describe("set") {
            it("selects the first template if null is set") {
                GuiActionRunner.execute { tree.selectedScheme = null }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[0])
            }

            it("selects the root if null is set and there are no templates") {
                GuiActionRunner.execute {
                    list().templates = emptyList()
                    tree.reload()
                    tree.selectedScheme = null
                }

                assertThat(tree.selectedScheme).isNull()
            }

            it("selects the first template if the scheme cannot be found") {
                GuiActionRunner.execute { tree.selectedScheme = DummyScheme.from("inside") }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[0])
            }

            it("selects the root if the scheme cannot be found and there are not templates") {
                GuiActionRunner.execute {
                    list().templates = emptyList()
                    tree.reload()
                    tree.selectedScheme = DummyScheme.from("inside")
                }

                assertThat(tree.selectedScheme).isNull()
            }

            it("selects the given scheme if it exists") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[1].schemes[0])
            }
        }
    }

    describe("selectedTemplate") {
        describe("get") {
            it("returns null if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(tree.selectedTemplate).isNull()
            }

            it("returns null if the root is selected") {
                GuiActionRunner.execute { tree.selectionPath = model().getPathToRoot(root()) }

                assertThat(tree.selectedTemplate).isNull()
            }

            it("returns null if a non-template scheme is selected") {
                GuiActionRunner.execute {
                    tree.selectionPath = model().getPathToRoot(StateNode(list().templates[1]))
                }

                assertThat(tree.selectedTemplate).isEqualTo(list().templates[1])
            }

            it("returns the selected template otherwise") {
                GuiActionRunner.execute {
                    tree.selectionPath = model().getPathToRoot(StateNode(list().templates[0]))
                }

                assertThat(tree.selectedTemplate).isEqualTo(list().templates[0])
            }
        }

        describe("set") {
            it("selects the first template if null is set") {
                GuiActionRunner.execute { tree.selectedTemplate = null }

                assertThat(tree.selectedTemplate).isEqualTo(list().templates[0])
            }

            it("selects the root if null is set and there are no templates") {
                GuiActionRunner.execute {
                    list().templates = emptyList()
                    tree.reload()
                    tree.selectedTemplate = null
                }

                assertThat(tree.selectedTemplate).isNull()
            }

            it("selects the given template if it exists") {
                GuiActionRunner.execute { tree.selectedTemplate = list().templates[1] }

                assertThat(tree.selectedTemplate).isEqualTo(list().templates[1])
            }
        }
    }


    describe("reload") {
        it("synchronizes removed nodes") {
            GuiActionRunner.execute { list().templates = list().templates.take(2) }

            assertThat(GuiActionRunner.execute<Int> { tree.rowCount }).isEqualTo(8)
            GuiActionRunner.execute { tree.reload() }

            assertThat(GuiActionRunner.execute<Int> { tree.rowCount }).isEqualTo(5)
        }

        it("synchronizes added nodes") {
            GuiActionRunner.execute {
                list().templates = list().templates + Template("Battle", listOf(DummyScheme.from("white")))
            }

            assertThat(GuiActionRunner.execute<Int> { tree.rowCount }).isEqualTo(8)
            GuiActionRunner.execute { tree.reload() }

            assertThat(GuiActionRunner.execute<Int> { tree.rowCount }).isEqualTo(10)
        }

        it("retains the selected scheme") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

            GuiActionRunner.execute { tree.reload() }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[1].schemes[0])
        }

        it("selects the first template if the selected node was removed") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

            GuiActionRunner.execute {
                list().templates[1].schemes = emptyList()
                tree.reload()
            }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[0])
        }

        it("selects the first template if no node was selected before") {
            GuiActionRunner.execute { tree.clearSelection() }

            GuiActionRunner.execute { tree.reload() }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[0])
        }

        it("keeps collapsed nodes collapsed") {
            GuiActionRunner.execute { tree.collapseRow(3) }

            GuiActionRunner.execute { tree.reload() }

            assertThat(GuiActionRunner.execute<Boolean> { tree.isCollapsed(3) }).isTrue()
        }

        it("keeps expanded nodes expanded") {
            GuiActionRunner.execute { tree.expandRow(0) }
            assertThat(GuiActionRunner.execute<Boolean> { tree.isExpanded(0) }).isTrue()

            GuiActionRunner.execute { tree.reload() }

            assertThat(GuiActionRunner.execute<Boolean> { tree.isExpanded(0) }).isTrue()
        }

        it("expands new nodes") {
            GuiActionRunner.execute {
                list().templates = list().templates + Template("Battle", listOf(DummyScheme.from("white")))
            }

            GuiActionRunner.execute { tree.reload() }

            assertThat(GuiActionRunner.execute<Boolean> { tree.isExpanded(5) }).isTrue()
        }
    }


    describe("addScheme") {
        describe("unique name") {
            it("appends (1) if a scheme with the given name already exists") {
                GuiActionRunner.execute { tree.addScheme(Template(name = "Captain")) }

                assertThat(list().templates[1].name).isEqualTo("Captain (1)")
            }

            it("appends (2) if two schemes with the given name already exist") {
                GuiActionRunner.execute {
                    tree.addScheme(Template(name = "Captain"))
                    tree.addScheme(Template(name = "Captain"))
                }

                assertThat(list().templates[2].name).isEqualTo("Captain (2)")
            }

            it("appends (2) if a scheme ending with (1) already exists") {
                GuiActionRunner.execute {
                    tree.addScheme(Template(name = "Captain (1)"))
                    tree.addScheme(Template(name = "Captain"))
                }

                assertThat(list().templates[2].name).isEqualTo("Captain (2)")
            }

            it("replaces (1) with (2) if a scheme ending with (1) already exists") {
                GuiActionRunner.execute {
                    tree.addScheme(Template(name = "Captain (1)"))
                    tree.addScheme(Template(name = "Captain (1)"))
                }

                assertThat(list().templates[2].name).isEqualTo("Captain (2)")
            }

            it("replaces (13) with (14) if a scheme ending with (13) already exists") {
                GuiActionRunner.execute {
                    tree.addScheme(Template(name = "Captain (13)"))
                    tree.addScheme(Template(name = "Captain (13)"))
                }

                assertThat(list().templates[2].name).isEqualTo("Captain (14)")
            }
        }


        it("inserts the template at the bottom if nothing is selected") {
            GuiActionRunner.execute { tree.clearSelection() }

            GuiActionRunner.execute { tree.addScheme(Template(name = "Yet")) }

            assertThat(list().templates.last().name).isEqualTo("Yet")
        }

        it("fails if a scheme is inserted while nothing is selected") {
            GuiActionRunner.execute { tree.clearSelection() }

            assertThatThrownBy { tree.addScheme(DummyScheme.from("arrow")) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot add non-template to root.")
        }

        it("inserts the template below the selected template") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1] }

            GuiActionRunner.execute { tree.addScheme(Template(name = "Future")) }

            assertThat(list().templates[2].name).isEqualTo("Future")
        }

        it("inserts the scheme at the bottom of the selected template") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1] }

            GuiActionRunner.execute { tree.addScheme(DummyScheme.from("content")) }

            assertThat(list().templates[1].schemes.last().name).isEqualTo("content")
        }

        it("inserts the template after the selected scheme") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[0].schemes[1] }

            GuiActionRunner.execute { tree.addScheme(Template(name = "Inch")) }

            assertThat(list().templates[1].name).isEqualTo("Inch")
        }

        it("inserts the scheme below the selected scheme") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[2].schemes[0] }

            GuiActionRunner.execute { tree.addScheme(DummyScheme.from("have")) }

            assertThat(list().templates[2].schemes[1].name).isEqualTo("have")
        }

        it("selects the inserted node") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

            GuiActionRunner.execute { tree.addScheme(DummyScheme.from("staff")) }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[1].schemes[1])
        }
    }

    describe("removeScheme") {
        it("removes the given node from the tree") {
            GuiActionRunner.execute { tree.removeScheme(list().templates[1]) }

            assertThat(list().templates.map { it.name }).containsExactly("Captain", "Village")
        }

        it("removes the selection if there is nothing to select") {
            GuiActionRunner.execute {
                repeat(3) {
                    tree.removeScheme(list().templates[0])
                }
            }

            assertThat(tree.selectedScheme).isNull()
        }

        it("selects the parent if the removed node had no siblings left") {
            GuiActionRunner.execute { tree.removeScheme(list().templates[1].schemes[0]) }

            assertThat(tree.selectedScheme?.name).isEqualTo("Particle")
        }

        it("selects the next sibling if the removed node has siblings") {
            GuiActionRunner.execute { tree.removeScheme(list().templates[0].schemes[0]) }

            assertThat(tree.selectedScheme?.name).isEqualTo("uncle")
        }
    }

    describe("moveSchemeByOnePosition") {
        it("moves a template") {
            GuiActionRunner.execute { tree.moveSchemeByOnePosition(list().templates[1], moveDown = true) }

            assertThat(list().templates.map { it.name }).containsExactly("Captain", "Village", "Particle")
        }

        it("moves a scheme") {
            GuiActionRunner.execute { tree.moveSchemeByOnePosition(list().templates[0].schemes[1], moveDown = false) }

            assertThat(list().templates[0].schemes.map { it.name }).containsExactly("uncle", "window")
        }

        it("expands and selects the moved template") {
            GuiActionRunner.execute { tree.moveSchemeByOnePosition(list().templates[2], moveDown = false) }

            assertThat(tree.selectedScheme?.name).isEqualTo("Village")
            assertThat(tree.isExpanded(model().getPathToRoot(StateNode(list().templates[1]))))
        }

        it("selects the moved scheme") {
            GuiActionRunner.execute { tree.moveSchemeByOnePosition(list().templates[2].schemes[0], moveDown = true) }

            assertThat(tree.selectedScheme?.name).isEqualTo("consider")
        }
    }

    describe("canMoveSchemeByOnePosition") {
        describe("nodeToRow") {
            it("returns 0 for the first template") {
                assertThat(model().nodeToRow(StateNode(list().templates[0]))).isEqualTo(0)
            }

            it("returns 1 for the first scheme") {
                assertThat(model().nodeToRow(StateNode(list().templates[0].schemes[0]))).isEqualTo(1)
            }

            it("returns the index considering non-collapsed nodes") {
                assertThat(model().nodeToRow(StateNode(list().templates[1]))).isEqualTo(3)
            }

            it("returns the index considering collapsed nodes") {
                GuiActionRunner.execute { tree.collapseRow(0) }

                assertThat(model().nodeToRow(StateNode(list().templates[1]))).isEqualTo(1)
            }
        }

        describe("rowToNode") {
            it("returns the first template for 0") {
                assertThat(model().rowToNode(0)).isEqualTo(StateNode(list().templates[0]))
            }

            it("returns the first scheme for 1") {
                assertThat(model().rowToNode(1)).isEqualTo(StateNode(list().templates[0].schemes[0]))
            }

            it("returns the scheme considering non-collapsed nodes") {
                assertThat(model().rowToNode(3)).isEqualTo(StateNode(list().templates[1]))
            }

            it("returns the scheme considering collapsed nodes") {
                GuiActionRunner.execute { tree.collapseRow(0) }

                assertThat(model().rowToNode(1)).isEqualTo(StateNode(list().templates[1]))
            }
        }
    }


    describe("buttons") {
        lateinit var frame: FrameFixture


        beforeEach {
            frame = Containers.showInFrame(GuiActionRunner.execute<JPanel> { tree.asDecoratedPanel() })
        }

        afterEach {
            frame.cleanUp()
        }


        fun updateButtons() = (frame.getActionButton("Add").parent as ActionToolbar).updateActionsImmediately()


        xdescribe("AddButton") {
            // No non-popup behavior to test
        }

        describe("RemoveButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    updateButtons()
                }

                assertThat(frame.getActionButton("Remove").isEnabled).isFalse()
            }

            it("removes the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0].schemes[1] }

                GuiActionRunner.execute { frame.getActionButton("Remove").click() }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("window")
            }
        }

        describe("CopyButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    updateButtons()
                }

                assertThat(frame.getActionButton("Copy").isEnabled).isFalse()
            }

            it("copies the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

                GuiActionRunner.execute { frame.getActionButton("Copy").click() }

                assertThat(list().templates[1].schemes.map { it.name }).containsExactly("republic", "republic")
            }

            it("creates an independent copy of the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[2].schemes[1] }

                GuiActionRunner.execute { frame.getActionButton("Copy").click() }
                (list().templates[2].schemes[2] as DummyScheme).literals = listOf("desert")

                assertThat(list().templates[2].schemes.map { it.name }).containsExactly("consider", "tent", "desert")
            }

            it("ensures the copy uses the same settings state") {
                val referredScheme = StringScheme()
                val referredTemplate = Template("break", listOf(referredScheme))

                val referenceScheme = TemplateReference(referredTemplate.uuid)
                val referenceTemplate = Template("airplane", listOf(referenceScheme))
                referenceTemplate.setStateContext(currentState)

                GuiActionRunner.execute {
                    list().templates = listOf(referredTemplate, referenceTemplate)
                    tree.reload()
                }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[1].schemes[0]
                    frame.getActionButton("Copy").click()
                }

                val selectedScheme = tree.selectedScheme!! as TemplateReference
                assertThat(selectedScheme).isNotSameAs(referenceScheme)
                assertThat(selectedScheme.template).isSameAs(referredTemplate)
            }
        }

        describe("UpButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    updateButtons()
                }

                assertThat(frame.getActionButton("Up").isEnabled).isFalse()
            }

            it("is disabled if the selected scheme cannot be moved up") {
                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    updateButtons()
                }

                assertThat(frame.getActionButton("Up").isEnabled).isFalse()
            }

            it("is enabled if the selected scheme can be moved up") {
                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[1]
                    updateButtons()
                }

                assertThat(frame.getActionButton("Up").isEnabled).isTrue()
            }

            it("moves the selected scheme up") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0].schemes[1] }

                GuiActionRunner.execute { frame.getActionButton("Up").click() }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("uncle", "window")
            }
        }

        describe("DownButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    updateButtons()
                }

                assertThat(frame.getActionButton("Down").isEnabled).isFalse()
            }

            it("is disabled if the selected scheme cannot be moved down") {
                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[2]
                    updateButtons()
                }

                assertThat(frame.getActionButton("Down").isEnabled).isFalse()
            }

            it("is enabled if the selected scheme can be moved down") {
                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[1]
                    updateButtons()
                }

                assertThat(frame.getActionButton("Down").isEnabled).isTrue()
            }

            it("moves the selected scheme down") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[2].schemes[0] }

                GuiActionRunner.execute { frame.getActionButton("Down").click() }

                assertThat(list().templates[2].schemes.map { it.name }).containsExactly("tent", "consider")
            }
        }

        describe("ResetButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    updateButtons()
                }

                assertThat(frame.getActionButton("Reset").isEnabled).isFalse()
            }

            it("is disabled if the selection has not been modified") {
                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    updateButtons()
                }

                assertThat(frame.getActionButton("Reset").isEnabled).isFalse()
            }

            it("removes the scheme if it was newly added") {
                list().templates = listOf(Template("Headache"))
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.getActionButton("Reset").click()
                }

                assertThat(list().templates).isEmpty()
            }

            it("resets changes to the initially selected scheme") {
                (currentState.templateList.templates[0].schemes[0] as DummyScheme).literals = listOf("approve")

                GuiActionRunner.execute { frame.getActionButton("Reset").click() }

                assertThat((currentState.templateList.templates[0].schemes[0] as DummyScheme).name).isEqualTo("window")
            }

            it("resets changes to a template") {
                list().templates[0].name = "Know"
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.getActionButton("Reset").click()
                }

                assertThat(list().templates[0].name).isEqualTo("Captain")
            }

            it("resets changes to a scheme") {
                (list().templates[0].schemes[0] as DummyScheme).literals = listOf("fly")
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0].schemes[0]
                    frame.getActionButton("Reset").click()
                }

                assertThat(list().templates[0].schemes[0].name).isEqualTo("window")
            }

            it("resets a template's scheme order") {
                list().templates[0].schemes = list().templates[0].schemes.reversed()
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.getActionButton("Reset").click()
                }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("window", "uncle")
            }

            it("resets a template's schemes") {
                (list().templates[2].schemes[0] as DummyScheme).literals = listOf("stir")
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[2]
                    frame.getActionButton("Reset").click()
                }

                assertThat(list().templates[2].schemes[0].name).isEqualTo("consider")
            }
        }
    }
})
