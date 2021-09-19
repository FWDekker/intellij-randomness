package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.SettingsState
import com.fwdekker.randomness.clickActionButton
import com.fwdekker.randomness.getActionButton
import com.fwdekker.randomness.string.StringScheme
import com.fwdekker.randomness.string.SymbolSet
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import com.fwdekker.randomness.word.UserDictionary
import com.fwdekker.randomness.word.WordScheme
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.FailOnThreadViolationRepaintManager
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.JPanel
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener


/**
 * GUI tests for [TemplateJTree].
 */
object TemplateJTreeTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture

    lateinit var originalState: SettingsState
    lateinit var currentState: SettingsState
    lateinit var tree: TemplateJTree

    fun model() = tree.myModel
    fun root() = tree.myModel.root
    fun list() = tree.myModel.list


    beforeGroup {
        FailOnThreadViolationRepaintManager.install()
    }

    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        originalState =
            SettingsState(
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

    afterEachTest {
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
            it("selects the first leaf if null is set") {
                GuiActionRunner.execute { tree.selectedScheme = null }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[0].schemes[0])
            }

            it("selects the first leaf if the scheme cannot be found") {
                GuiActionRunner.execute { tree.selectedScheme = DummyScheme.from("inside") }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[0].schemes[0])
            }

            it("selects the given scheme if it exists") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

                assertThat(tree.selectedScheme).isEqualTo(list().templates[1].schemes[0])
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

        it("selects the first leaf if the selected node was removed") {
            GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

            GuiActionRunner.execute {
                list().templates[1].schemes = emptyList()
                tree.reload()
            }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[0].schemes[0])
        }

        it("selects the first leaf if no node was selected before") {
            GuiActionRunner.execute { tree.clearSelection() }

            GuiActionRunner.execute { tree.reload() }

            assertThat(tree.selectedScheme).isEqualTo(list().templates[0].schemes[0])
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


        beforeEachTest {
            frame = Containers.showInFrame(GuiActionRunner.execute<JPanel> { tree.asDecoratedPanel() })
        }

        afterEachTest {
            frame.cleanUp()
        }


        describe("AddButton") {
            it("immediately adds a template if the tree is empty") {
                GuiActionRunner.execute {
                    list().templates = emptyList()
                    tree.reload()
                }

                GuiActionRunner.execute { frame.clickActionButton("Add") }

                assertThat(list().templates).hasSize(1)
            }
        }

        describe("RemoveButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(frame.getActionButton("Remove").isEnabled).isFalse()
            }

            it("removes the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0].schemes[1] }

                GuiActionRunner.execute { frame.clickActionButton("Remove") }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("window")
            }
        }

        describe("CopyButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(frame.getActionButton("Copy").isEnabled).isFalse()
            }

            it("copies the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[1].schemes[0] }

                GuiActionRunner.execute { frame.clickActionButton("Copy") }

                assertThat(list().templates[1].schemes.map { it.name }).containsExactly("republic", "republic")
            }

            it("creates an independent copy of the selected scheme") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[2].schemes[1] }

                GuiActionRunner.execute { frame.clickActionButton("Copy") }
                (list().templates[2].schemes[2] as DummyScheme).literals = listOf("desert")

                assertThat(list().templates[2].schemes.map { it.name }).containsExactly("consider", "tent", "desert")
            }

            it("ensures the copy uses the same settings state") {
                val scheme = StringScheme().also { it.setSettingsState(currentState) }
                GuiActionRunner.execute {
                    list().templates[0].schemes = listOf(scheme)
                    tree.reload()
                }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0].schemes[0]
                    frame.clickActionButton("Copy")
                }
                (+scheme.symbolSetSettings).symbolSets = listOf(SymbolSet("deliver", "lhO"))

                assertThat((+(list().templates[0].schemes[1] as StringScheme).symbolSetSettings).symbolSets)
                    .containsExactly(SymbolSet("deliver", "lhO"))
            }
        }

        describe("UpButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(frame.getActionButton("Up").isEnabled).isFalse()
            }

            it("is disabled if the selected scheme cannot be moved up") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0] }

                assertThat(frame.getActionButton("Up").isEnabled).isFalse()
            }

            it("moves the selected scheme up") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0].schemes[1] }

                GuiActionRunner.execute { frame.clickActionButton("Up") }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("uncle", "window")
            }
        }

        describe("DownButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(frame.getActionButton("Down").isEnabled).isFalse()
            }

            it("is disabled if the selected scheme cannot be moved down") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[2] }

                assertThat(frame.getActionButton("Down").isEnabled).isFalse()
            }

            it("moves the selected scheme down") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[2].schemes[0] }

                GuiActionRunner.execute { frame.clickActionButton("Down") }

                assertThat(list().templates[2].schemes.map { it.name }).containsExactly("tent", "consider")
            }
        }

        describe("ResetButton") {
            it("is disabled if nothing is selected") {
                GuiActionRunner.execute { tree.clearSelection() }

                assertThat(frame.getActionButton("Reset").isEnabled).isFalse()
            }

            it("is disabled if the selection has not been modified") {
                GuiActionRunner.execute { tree.selectedScheme = list().templates[0] }

                assertThat(frame.getActionButton("Reset").isEnabled).isFalse()
            }

            it("removes the scheme if it was newly added") {
                list().templates = listOf(Template("Headache"))
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.clickActionButton("Reset")
                }

                assertThat(list().templates).isEmpty()
            }

            it("resets changes to the initially selected scheme") {
                (currentState.templateList.templates[0].schemes[0] as DummyScheme).literals = listOf("approve")

                GuiActionRunner.execute { frame.clickActionButton("Reset") }

                assertThat((currentState.templateList.templates[0].schemes[0] as DummyScheme).name).isEqualTo("window")
            }

            it("resets changes to a template") {
                list().templates[0].name = "Know"
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.clickActionButton("Reset")
                }

                assertThat(list().templates[0].name).isEqualTo("Captain")
            }

            it("resets changes to a scheme") {
                (list().templates[0].schemes[0] as DummyScheme).literals = listOf("fly")
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0].schemes[0]
                    frame.clickActionButton("Reset")
                }

                assertThat(list().templates[0].schemes[0].name).isEqualTo("window")
            }

            it("resets a template's scheme order") {
                list().templates[0].schemes = list().templates[0].schemes.reversed()
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0]
                    frame.clickActionButton("Reset")
                }

                assertThat(list().templates[0].schemes.map { it.name }).containsExactly("window", "uncle")
            }

            it("resets a template's schemes") {
                (list().templates[2].schemes[0] as DummyScheme).literals = listOf("stir")
                GuiActionRunner.execute { tree.reload() }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[2]
                    frame.clickActionButton("Reset")
                }

                assertThat(list().templates[2].schemes[0].name).isEqualTo("consider")
            }

            it("resets the symbol set settings if a string scheme is selected") {
                val scheme = StringScheme().also { it.setSettingsState(currentState) }
                GuiActionRunner.execute {
                    list().templates[0].schemes = listOf(scheme)
                    originalState.copyFrom(currentState)
                    tree.reload()
                }

                GuiActionRunner.execute {
                    tree.selectedScheme = scheme
                    (+scheme.symbolSetSettings).symbolSets = listOf(SymbolSet("hardly", "6i9HY9"))

                    frame.clickActionButton("Reset")
                }

                assertThat(currentState.symbolSetSettings.symbolSets)
                    .containsExactlyElementsOf(originalState.symbolSetSettings.symbolSets)
            }

            it("resets the dictionary settings if a word scheme is selected") {
                val scheme = WordScheme().also { it.setSettingsState(currentState) }
                GuiActionRunner.execute {
                    list().templates[0].schemes = listOf(scheme)
                    originalState.copyFrom(currentState)

                    tree.reload()
                }

                GuiActionRunner.execute {
                    tree.selectedScheme = list().templates[0].schemes[0]
                    (+scheme.dictionarySettings).dictionaries = listOf(UserDictionary("somebody.dic"))

                    frame.clickActionButton("Reset")
                }

                assertThat((+scheme.dictionarySettings).dictionaries)
                    .containsExactlyElementsOf(originalState.dictionarySettings.dictionaries)
            }
        }
    }
})


/**
 * Unit tests for [TemplateTreeModel].
 */
object TemplateTreeModelTest : Spek({
    lateinit var model: TemplateTreeModel


    beforeEachTest {
        model = TemplateTreeModel(
            TemplateList(
                listOf(
                    Template("Strong", listOf(DummyScheme.from("bell"), DummyScheme.from("people"))),
                    Template("Roll", listOf(DummyScheme.from("hot"))),
                    Template("Steady", emptyList())
                )
            )
        )
    }


    describe("rowToNode (default implementation)") {
        it("returns null for a negative index") {
            assertThat(model.rowToNode(-2)).isNull()
        }

        it("returns null at a too-high index") {
            assertThat(model.rowToNode(241)).isNull()
        }

        it("returns the first template given index 0") {
            assertThat(model.rowToNode(0)).isEqualTo(model.root.children[0])
        }

        it("returns the first leaf given index 1") {
            assertThat(model.rowToNode(1)).isEqualTo(model.root.children[0].children[0])
        }

        it("returns the second template given an index considering the number of previous templates") {
            assertThat(model.rowToNode(5)).isEqualTo(model.root.children[2])
        }

        it("returns a scheme of the second template given an index considering the number of previous schemes") {
            assertThat(model.rowToNode(4)).isEqualTo(model.root.children[1].children[0])
        }
    }

    describe("nodeToRow (default implementation)") {
        it("returns -1 for null") {
            assertThat(model.nodeToRow(null)).isEqualTo(-1)
        }

        it("returns -1 for an unknown node") {
            assertThat(model.nodeToRow(StateNode(DummyScheme()))).isEqualTo(-1)
        }

        it("returns 0 for the first template") {
            assertThat(model.nodeToRow(model.root.children[0])).isEqualTo(0)
        }

        it("returns 1 for the first scheme") {
            assertThat(model.nodeToRow(model.root.children[0].children[0])).isEqualTo(1)
        }

        it("returns the index considering the number of previous templates given the second template") {
            assertThat(model.nodeToRow(model.root.children[2])).isEqualTo(5)
        }

        it("returns an index considering the number of previous schemes given a scheme of the second template") {
            assertThat(model.nodeToRow(model.root.children[1].children[0])).isEqualTo(4)
        }
    }


    describe("reload") {
        it("informs listeners that the root has been reloaded") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val oldList = model.list
            model.reload()

            assertThat(model.list).isEqualTo(oldList)
            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            assertThat(lastEvent!!.childIndices).isEmpty()
            assertThat(lastEvent!!.children).isNull()
        }

        it("informs listeners that a new root has been loaded") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val newList = TemplateList(emptyList())
            model.reload(newList)

            assertThat(model.list).isEqualTo(newList)
            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(StateNode(newList))
            assertThat(lastEvent!!.childIndices).isEmpty()
            assertThat(lastEvent!!.children).isNull()
        }
    }


    describe("addRow") {
        it("throws an error") {
            assertThatThrownBy { model.addRow() }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot add empty row.")
        }
    }

    describe("removeRow") {
        it("throws an error") {
            assertThatThrownBy { model.removeRow(641) }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot remove row by index.")
        }
    }

    describe("exchangeRows") {
        describe("templates") {
            it("moves a template to the previous template") {
                model.exchangeRows(5, 3)

                assertThat(model.list.templates.map { it.name }).containsExactly("Strong", "Steady", "Roll")
            }

            it("moves a template to the next template") {
                model.exchangeRows(0, 3)

                assertThat(model.list.templates.map { it.name }).containsExactly("Roll", "Strong", "Steady")
            }

            it("moves a template to the next-template-but-one") {
                model.exchangeRows(0, 5)

                assertThat(model.list.templates.map { it.name }).containsExactly("Roll", "Steady", "Strong")
            }
        }

        describe("schemes") {
            it("moves a scheme to the previous scheme under the same parent") {
                model.exchangeRows(1, 2)

                assertThat(model.list.templates[0].schemes.map { it.name }).containsExactly("people", "bell")
            }

            it("moves a scheme to the next scheme under the same parent") {
                model.exchangeRows(2, 1)

                assertThat(model.list.templates[0].schemes.map { it.name }).containsExactly("people", "bell")
            }

            it("moves a scheme to its parent, making it the last child of the parent's previous sibling") {
                model.exchangeRows(4, 3)

                assertThat(model.list.templates[0].schemes.map { it.name }).containsExactly("bell", "people", "hot")
                assertThat(model.list.templates[1].schemes).isEmpty()
            }

            it("moves a scheme to its parent's next sibling, making it that sibling's first child") {
                model.exchangeRows(4, 5)

                assertThat(model.list.templates[1].schemes).isEmpty()
                assertThat(model.list.templates[2].schemes.map { it.name }).containsExactly("hot")
            }

            it("moves a scheme to a scheme in another template") {
                model.exchangeRows(1, 4)

                assertThat(model.list.templates[0].schemes.map { it.name }).containsExactly("people")
                assertThat(model.list.templates[1].schemes.map { it.name }).containsExactly("bell", "hot")
            }
        }
    }

    describe("canExchangeRows") {
        it("returns false if the old node could not be found") {
            assertThat(model.canExchangeRows(-2, 2)).isFalse()
        }

        it("returns false if the new node could not be found") {
            assertThat(model.canExchangeRows(1, -4)).isFalse()
        }

        it("returns false if the old node is a template but the new node is a non-template scheme") {
            assertThat(model.canExchangeRows(0, 2)).isFalse()
        }

        it("returns false if the old node is a non-template scheme and the new node is the first template") {
            assertThat(model.canExchangeRows(1, 0)).isFalse()
        }

        it("returns true if the nodes at both indices are templates") {
            assertThat(model.canExchangeRows(0, 5)).isTrue()
        }

        it("returns true if the nodes at both indices are schemes") {
            assertThat(model.canExchangeRows(1, 4)).isTrue()
        }

        it("returns true if the old node is a scheme and the new node is a non-first template") {
            assertThat(model.canExchangeRows(4, 3)).isTrue()
        }
    }


    describe("isLeaf") {
        it("throws an error if the given node is not a StateNode") {
            assertThatThrownBy { model.isLeaf("enter") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("'node' must be a StateNode, but was a 'java.lang.String'.")
        }

        it("returns true if the node cannot have children") {
            assertThat(model.isLeaf(StateNode(DummyScheme()))).isTrue()
        }

        it("returns true if the node can have children but has no children") {
            assertThat(model.isLeaf(StateNode(Template()))).isTrue()
        }

        it("returns false if the node can have children and has children") {
            assertThat(model.isLeaf(StateNode(Template(schemes = listOf(DummyScheme()))))).isFalse()
        }
    }

    describe("getChild") {
        it("throws an error if the given node is not a StateNode") {
            assertThatThrownBy { model.getChild("over", 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("'parent' must be a StateNode, but was a 'java.lang.String'.")
        }

        it("throws an error if the given node cannot have children") {
            assertThatThrownBy { model.getChild(StateNode(DummyScheme()), 0) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot get child of parent that cannot have children.")
        }

        it("throws an error if there is no child at the given index") {
            val template = Template(schemes = listOf(DummyScheme()))

            assertThatThrownBy { model.getChild(StateNode(template), 2) }
                .isInstanceOf(IndexOutOfBoundsException::class.java)
        }

        it("returns the child at the given index") {
            val template = Template(schemes = listOf(DummyScheme.from("appear"), DummyScheme.from("tribe")))

            assertThat(model.getChild(StateNode(template), 1).state).isEqualTo(template.schemes[1])
        }
    }

    describe("getChildCount") {
        it("throws an error if the given node is not a StateNode") {
            assertThatThrownBy { model.getChildCount("eager") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("'parent' must be a StateNode, but was a 'java.lang.String'.")
        }

        it("returns 0 if the node cannot have children") {
            assertThat(model.getChildCount(StateNode(DummyScheme()))).isZero()
        }

        it("returns 0 if the node has no children") {
            assertThat(model.getChildCount(StateNode(Template()))).isZero()
        }

        it("returns the number of children of the node") {
            val template = Template(schemes = listOf(DummyScheme(), DummyScheme()))

            assertThat(model.getChildCount(StateNode(template))).isEqualTo(2)
        }
    }

    describe("getIndexOfChild") {
        it("returns -1 if the parent is null") {
            assertThat(model.getIndexOfChild(null, StateNode(DummyScheme()))).isEqualTo(-1)
        }

        it("returns -1 if the child is null") {
            assertThat(model.getIndexOfChild(StateNode(DummyScheme()), null)).isEqualTo(-1)
        }

        it("throws an error if the parent is not a StateNode") {
            assertThatThrownBy { model.getIndexOfChild("mild", StateNode(DummyScheme())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("'parent' must be a StateNode, but was a 'java.lang.String'.")
        }

        it("throws an error if the child is not a StateNode") {
            assertThatThrownBy { model.getIndexOfChild(StateNode(DummyScheme()), "soldier") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("'child' must be a StateNode, but was a 'java.lang.String'.")
        }

        it("returns -1 if the child is not contained in the parent") {
            val child = DummyScheme()
            val parent = Template(schemes = emptyList())

            assertThat(model.getIndexOfChild(StateNode(parent), StateNode(child))).isEqualTo(-1)
        }

        it("returns the index of the child in the parent") {
            val child = DummyScheme()
            val parent = Template(schemes = listOf(DummyScheme.from("advance"), child, DummyScheme.from("then")))

            assertThat(model.getIndexOfChild(StateNode(parent), StateNode(child))).isEqualTo(1)
        }

        it("returns the index of the child in the parent considering only the child's UUID") {
            val child = DummyScheme()
            val parent = Template(schemes = listOf(DummyScheme.from("blood"), child, DummyScheme.from("private")))

            val childCopy = DummyScheme().also { it.uuid = child.uuid }
            assertThat(model.getIndexOfChild(StateNode(parent), StateNode(childCopy))).isEqualTo(1)
        }
    }

    describe("getParentOf") {
        it("returns an error if the node is not contained in this model") {
            assertThatThrownBy { model.getParentOf(StateNode(DummyScheme())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot get parent of node not in this model.")
        }

        it("returns null as the parent of the root") {
            assertThat(model.getParentOf(model.root)).isNull()
        }

        it("returns the root as the parent of any template") {
            val template = Template(name = "Talk")
            model.reload(TemplateList(listOf(template)))

            assertThat(model.getParentOf(StateNode(template))).isEqualTo(model.root)
        }

        it("returns the appropriate template as the parent of a given scheme") {
            val scheme = DummyScheme.from("everyone")
            val template = Template("Snake", listOf(scheme))
            model.reload(TemplateList(listOf(Template("Firm", listOf(DummyScheme.from("set"))), template)))

            assertThat(model.getParentOf(StateNode(scheme))).isEqualTo(StateNode(template))
        }
    }

    describe("getPathToRoot") {
        it("returns an error if the node is not contained in this model") {
            assertThatThrownBy { model.getPathToRoot(StateNode(DummyScheme())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot get path of node not in this model.")
        }

        it("returns a path `root` if a path from the root is requested") {
            assertThat(model.getPathToRoot(model.root).path).containsExactly(model.root)
        }

        it("returns a path `root -> template` if a path from a template is requested") {
            val template = Template(name = "Package")
            model.reload(TemplateList(listOf(template)))

            assertThat(model.getPathToRoot(StateNode(template)).path).containsExactly(model.root, StateNode(template))
        }

        it("returns a path `root -> template -> scheme` if a path from a scheme is requested") {
            val scheme = DummyScheme.from("review")
            val template = Template("Reserve", listOf(scheme))
            model.reload(TemplateList(listOf(template)))

            assertThat(model.getPathToRoot(StateNode(scheme)).path)
                .containsExactly(model.root, StateNode(template), StateNode(scheme))
        }
    }

    describe("getFirstLeaf") {
        it("returns the template list if there are no templates") {
            model.reload(TemplateList(emptyList()))

            assertThat(model.getFirstLeaf()).isEqualTo(StateNode(model.list))
        }

        it("returns the first template if it has no schemes") {
            val template = Template(name = "Stem")
            model.reload(TemplateList(listOf(template)))

            assertThat(model.getFirstLeaf()).isEqualTo(StateNode(template))
        }

        it("returns the first scheme of the first template otherwise") {
            val scheme = DummyScheme.from("rid")
            model.reload(TemplateList(listOf(Template("Enemy", listOf(scheme)))))

            assertThat(model.getFirstLeaf()).isEqualTo(StateNode(scheme))
        }
    }


    describe("insertNode") {
        it("throws an error when a node is inserted at a negative index") {
            assertThatThrownBy { model.insertNode(model.root, StateNode(Template()), -2) }
                .isInstanceOf(IndexOutOfBoundsException::class.java)
        }

        it("throws an error if a node is inserted at a too-high index") {
            assertThatThrownBy { model.insertNode(model.root, StateNode(Template()), 14) }
                .isInstanceOf(IndexOutOfBoundsException::class.java)
        }

        it("inserts a node into the model and the underlying list") {
            model.reload(TemplateList(listOf(Template(name = "Current"), Template(name = "Here"))))

            val template = Template("Royal", listOf(DummyScheme.from("aim")))
            model.insertNode(model.root, StateNode(template), 1)

            assertThat(model.root.children).hasSize(3)
            assertThat(model.root.children[1]).isEqualTo(StateNode(template))
            assertThat(model.list.templates).hasSize(3)
            assertThat(model.list.templates[1]).isEqualTo(template)
        }

        it("inserts a node as the last child of a parent") {
            model.reload(TemplateList(listOf(Template(name = "Loud"), Template(name = "Various"))))

            val template = Template(name = "Sister")
            model.insertNode(model.root, StateNode(template))

            assertThat(model.root.children[2]).isEqualTo(StateNode(template))
            assertThat(model.list.templates[2]).isEqualTo(template)
        }

        it("informs listeners of the insertion") {
            model.reload(TemplateList(listOf(Template(name = "Rather"))))

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val template = Template(name = "Fortune")
            model.insertNode(model.root, StateNode(template))

            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            assertThat(lastEvent!!.childIndices).containsExactly(1)
            assertThat(lastEvent!!.children).containsExactly(StateNode(template))
        }
    }

    describe("insertNodeAfter") {
        it("throws an error if the node to insert after is not in the parent") {
            assertThatThrownBy { model.insertNodeAfter(model.root, StateNode(Template()), StateNode(DummyScheme())) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot find node to insert after in parent.")
        }

        it("inserts the node after the given node") {
            model.reload(TemplateList(listOf(Template(name = "Aloud"), Template(name = "Homemade"))))

            val template = Template(name = "Family")
            model.insertNodeAfter(model.root, StateNode(template), model.root.children[1])

            assertThat(model.list.templates[2]).isEqualTo(template)
        }
    }

    describe("removeNode") {
        it("throws an error if the given node is not contained in the model") {
            assertThatThrownBy { model.removeNode(StateNode(DummyScheme.from("best"))) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot remove node not contained in this model.")
        }

        it("throws an error if the given node is the root of the model") {
            assertThatThrownBy { model.removeNode(model.root) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessage("Cannot remove root from model.")
        }

        it("removes a node from the model and the underlying list") {
            val template = Template(name = "Ride")
            model.reload(TemplateList(listOf(Template(name = "Animal"), template, Template(name = "Tend"))))

            model.removeNode(StateNode(template))

            assertThat(model.root.children)
                .hasSize(2)
                .doesNotContain(StateNode(template))
            assertThat(model.list.templates)
                .hasSize(2)
                .doesNotContain(template)
        }

        it("informs listeners of the removal") {
            val template = Template(name = "Fortune")
            model.reload(TemplateList(listOf(Template(name = "Rather"), template)))

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })
            model.removeNode(StateNode(template))

            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            assertThat(lastEvent!!.childIndices).containsExactly(1)
            assertThat(lastEvent!!.children).containsExactly(StateNode(template))
        }
    }


    describe("fire methods") {
        var nodesChangedInvoked = 0
        var nodesInsertedInvoked = 0
        var nodesRemovedInvoked = 0
        var structureChangedInvoked = 0
        var totalInvoked = 0
        var lastEvent: TreeModelEvent? = null


        beforeEachTest {
            nodesChangedInvoked = 0
            nodesInsertedInvoked = 0
            nodesRemovedInvoked = 0
            structureChangedInvoked = 0
            totalInvoked = 0
            lastEvent = null

            model.addTreeModelListener(object : TreeModelListener {
                override fun treeNodesChanged(event: TreeModelEvent) {
                    nodesChangedInvoked++
                    totalInvoked++
                    lastEvent = event
                }

                override fun treeNodesInserted(event: TreeModelEvent) {
                    nodesInsertedInvoked++
                    totalInvoked++
                    lastEvent = event
                }

                override fun treeNodesRemoved(event: TreeModelEvent) {
                    nodesRemovedInvoked++
                    totalInvoked++
                    lastEvent = event
                }

                override fun treeStructureChanged(event: TreeModelEvent) {
                    structureChangedInvoked++
                    totalInvoked++
                    lastEvent = event
                }
            })
        }


        describe("fireNodeChanged") {
            it("does nothing if the given node is null") {
                model.fireNodeChanged(null)

                assertThat(nodesChangedInvoked).isZero()
                assertThat(totalInvoked - nodesChangedInvoked).isZero()
            }

            it("invokes the listener with an event without children if the current root is provided") {
                model.fireNodeChanged(model.root)

                assertThat(nodesChangedInvoked).isOne()
                assertThat(totalInvoked - nodesChangedInvoked).isZero()
                assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            }

            it("indicates the parent and child index when a non-root node is changed") {
                val template = model.list.templates[0]

                model.fireNodeChanged(StateNode(template))

                assertThat(nodesChangedInvoked).isOne()
                assertThat(totalInvoked - nodesChangedInvoked).isZero()
                assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
                assertThat(lastEvent!!.childIndices).containsExactly(0)
                assertThat(lastEvent!!.children).containsExactly(StateNode(template))
            }
        }

        describe("fireNodeInserted") {
            it("does nothing if the given node is null") {
                model.fireNodeInserted(null, StateNode(DummyScheme()), 227)

                assertThat(totalInvoked).isZero()
            }

            it("throws an error if a template list is given") {
                assertThatThrownBy { model.fireNodeInserted(model.root, StateNode(DummyScheme()), 688) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Template list cannot have parent so cannot be inserted.")
            }

            it("fires an event with the given parent, node, and index") {
                val template = model.list.templates[0]
                val newScheme = DummyScheme.from("show")

                model.fireNodeInserted(StateNode(newScheme), StateNode(template), 1)

                assertThat(nodesInsertedInvoked).isOne()
                assertThat(totalInvoked - nodesInsertedInvoked).isZero()
                assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(StateNode(template))
                assertThat(lastEvent!!.childIndices).containsExactly(1)
                assertThat(lastEvent!!.children).containsExactly(StateNode(newScheme))
            }
        }

        describe("fireNodeRemoved") {
            it("does nothing if the given node is null") {
                model.fireNodeRemoved(null, StateNode(DummyScheme()), 247)

                assertThat(totalInvoked).isZero()
            }

            it("throws an error if a template list is given") {
                assertThatThrownBy { model.fireNodeRemoved(model.root, StateNode(DummyScheme()), 888) }
                    .isInstanceOf(IllegalArgumentException::class.java)
                    .hasMessage("Template list cannot have parent so cannot be removed.")
            }

            it("fires an event with the given parent, node, and index") {
                val template = model.list.templates[0]
                val oldScheme = template.schemes[0]

                model.fireNodeRemoved(StateNode(oldScheme), StateNode(template), 0)

                assertThat(nodesRemovedInvoked).isOne()
                assertThat(totalInvoked - nodesRemovedInvoked).isZero()
                assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(StateNode(template))
                assertThat(lastEvent!!.childIndices).containsExactly(0)
                assertThat(lastEvent!!.children).containsExactly(StateNode(oldScheme))
            }
        }

        describe("fireNodeStructureChanged") {
            it("does nothing if the given node is null") {
                model.fireNodeStructureChanged(null)

                assertThat(totalInvoked).isZero()
            }

            it("fires an event with the given node") {
                val template = model.list.templates[0]
                model.fireNodeStructureChanged(StateNode(template))

                assertThat(structureChangedInvoked).isOne()
                assertThat(totalInvoked - structureChangedInvoked).isZero()
                assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(StateNode(template))
                assertThat(lastEvent!!.childIndices).isEmpty()
                assertThat(lastEvent!!.children).isNull()
            }
        }
    }


    describe("valueForPathChanged") {
        it("throws an error") {
            assertThatThrownBy { model.valueForPathChanged(model.getPathToRoot(model.root), "dirt") }
                .isInstanceOf(IllegalStateException::class.java)
                .hasMessage("Cannot change value by path.")
        }
    }

    describe("addTreeModelListener") {
        it("invokes the added listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.reload()

            assertThat(invoked).isEqualTo(1)
        }
    }

    describe("removeTreeModelListener") {
        it("no longer invokes the removed listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.reload()

            model.removeTreeModelListener(listener)
            model.reload()

            assertThat(invoked).isEqualTo(1)
        }
    }
})

/**
 * Unit tests for [StateNode].
 */
object StateNodeTest : Spek({
    describe("canHaveChildren") {
        it("returns true if the state is a template list") {
            assertThat(StateNode(TemplateList(emptyList())).canHaveChildren).isTrue()
        }

        it("returns true if the state is a template") {
            assertThat(StateNode(Template()).canHaveChildren).isTrue()
        }

        it("returns false if the state is a non-template scheme") {
            assertThat(StateNode(DummyScheme()).canHaveChildren).isFalse()
        }
    }

    describe("children") {
        describe("get") {
            it("returns the templates of a template list") {
                val templates = listOf(Template(name = "Hammer"), Template(name = "Shadow"))

                assertThat(StateNode(TemplateList(templates)).children.map { it.state })
                    .containsExactlyElementsOf(templates)
            }

            it("returns the schemes of a template") {
                val schemes = listOf(DummyScheme.from("virtue"), DummyScheme.from("shock"))

                assertThat(StateNode(Template(schemes = schemes)).children.map { it.state })
                    .containsExactlyElementsOf(schemes)
            }

            it("throws an error for a non-template scheme") {
                assertThatThrownBy { StateNode(DummyScheme()).children }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Unknown parent type 'com.fwdekker.randomness.DummyScheme'.")
            }
        }

        describe("set") {
            it("modifies the templates of a template list") {
                val list = TemplateList(listOf(Template(name = "Wave"), Template(name = "Limit")))

                StateNode(list).children = listOf(StateNode(Template(name = "Meantime")))

                assertThat(list.templates.map { it.name }).containsExactly("Meantime")
            }

            it("modifies the schemes of a template") {
                val template = Template(schemes = listOf(DummyScheme.from("mean"), DummyScheme.from("further")))

                StateNode(template).children = listOf(StateNode(DummyScheme.from("scenery")))

                assertThat(template.schemes.map { it.name }).containsExactly("scenery")
            }

            it("throws an error for a non-template scheme") {
                assertThatThrownBy { StateNode(DummyScheme()).children = emptyList() }
                    .isInstanceOf(IllegalStateException::class.java)
                    .hasMessage("Unknown parent type 'com.fwdekker.randomness.DummyScheme'.")
            }
        }
    }

    describe("recursiveChildren") {
        it("returns the templates and schemes of a template list in depth-first order") {
            val templates = listOf(
                Template("Hammer", listOf(DummyScheme.from("absence"), DummyScheme.from("like"))),
                Template("Shadow", listOf(DummyScheme.from("village")))
            )

            assertThat(StateNode(TemplateList(templates)).recursiveChildren.map { (it.state as Scheme).name })
                .containsExactly("Hammer", "absence", "like", "Shadow", "village")
        }

        it("returns the schemes of a template") {
            val schemes = listOf(DummyScheme.from("ache"), DummyScheme.from("future"))

            assertThat(StateNode(Template(schemes = schemes)).recursiveChildren.map { it.state })
                .containsExactlyElementsOf(schemes)
        }

        it("returns an empty list if the node cannot have children") {
            assertThat(StateNode(DummyScheme()).recursiveChildren).isEmpty()
        }
    }


    describe("contains") {
        describe("template list") {
            it("returns true if the given template list is itself") {
                val node = StateNode(TemplateList(emptyList()))

                assertThat(node.contains(node)).isTrue()
            }

            it("returns false if the given template list is not itself") {
                val nodeA = StateNode(TemplateList(emptyList()))
                val nodeB = StateNode(TemplateList(emptyList()))

                assertThat(nodeA.contains(nodeB)).isFalse()
            }

            it("returns true if the given template is contained in the template list") {
                val template = Template(name = "Gain")
                val node = StateNode(TemplateList(listOf(template)))

                assertThat(node.contains(StateNode(template))).isTrue()
            }

            it("returns false if the given template is not contained in the template list") {
                val node = StateNode(TemplateList(listOf(Template(name = "Governor"))))

                assertThat(node.contains(StateNode(Template(name = "Resist")))).isFalse()
            }

            it("returns true if the given scheme is contained in the template list") {
                val scheme = DummyScheme.from("company")
                val node = StateNode(TemplateList(listOf(Template(schemes = listOf(scheme)))))

                assertThat(node.contains(StateNode(scheme))).isTrue()
            }

            it("returns false if the given scheme is not contained in the template list") {
                val node = StateNode(TemplateList(listOf(Template(schemes = listOf(DummyScheme.from("northern"))))))

                assertThat(node.contains(StateNode(DummyScheme.from("curve")))).isFalse()
            }
        }

        describe("template") {
            it("returns false for a template list") {
                val node = StateNode(Template())

                assertThat(node.contains(StateNode(TemplateList(emptyList())))).isFalse()
            }

            it("returns true if the given template is itself") {
                val node = StateNode(Template())

                assertThat(node.contains(node)).isTrue()
            }

            it("returns false if the given template is not itself") {
                val nodeA = StateNode(Template(name = "Imagine"))
                val nodeB = StateNode(Template(name = "Ideal"))

                assertThat(nodeA.contains(nodeB)).isFalse()
            }

            it("returns true if the given scheme is contained in the template") {
                val scheme = DummyScheme.from("borrow")
                val node = StateNode(Template(schemes = listOf(scheme)))

                assertThat(node.contains(StateNode(scheme))).isTrue()
            }

            it("returns false if the given scheme is not contained in the template") {
                val node = StateNode(Template(schemes = listOf(DummyScheme.from("also"))))

                assertThat(node.contains(StateNode(DummyScheme.from("enter")))).isFalse()
            }
        }

        describe("scheme") {
            it("returns false for a template list") {
                val node = StateNode(Template())

                assertThat(node.contains(StateNode(TemplateList(emptyList())))).isFalse()
            }

            it("returns false for a template") {
                val node = StateNode(Template())

                assertThat(node.contains(StateNode(Template()))).isFalse()
            }

            it("returns true if the given scheme is itself") {
                val node = StateNode(DummyScheme.from("empire"))

                assertThat(node.contains(node)).isTrue()
            }

            it("returns false if the given scheme is not itself") {
                val nodeA = StateNode(DummyScheme.from("soft"))
                val nodeB = StateNode(DummyScheme.from("penny"))

                assertThat(nodeA.contains(nodeB)).isFalse()
            }
        }
    }


    describe("equals") {
        it("does not equal an object of another class") {
            assertThat(StateNode(DummyScheme())).isNotEqualTo(299)
        }

        it("equals itself") {
            val node = StateNode(DummyScheme())

            assertThat(node).isEqualTo(node)
        }

        it("equals an identical object") {
            val scheme = DummyScheme()

            assertThat(StateNode(scheme)).isEqualTo(StateNode(scheme))
        }

        it("does not equal a different object") {
            assertThat(StateNode(DummyScheme())).isNotEqualTo(StateNode(DummyScheme()))
        }
    }

    describe("hashCode") {
        it("equals the hashcode of itself") {
            val node = StateNode(DummyScheme())

            assertThat(node.hashCode()).isEqualTo(node.hashCode())
        }

        it("equals the hashcode of an identical object") {
            val scheme = DummyScheme()

            assertThat(StateNode(scheme).hashCode()).isEqualTo(StateNode(scheme).hashCode())
        }

        it("does not equal the hashcode of a different object") {
            assertThat(StateNode(DummyScheme()).hashCode()).isNotEqualTo(StateNode(DummyScheme()).hashCode())
        }
    }
})
