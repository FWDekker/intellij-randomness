package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import com.intellij.testFramework.fixtures.IdeaTestFixture
import com.intellij.testFramework.fixtures.IdeaTestFixtureFactory
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.assertj.swing.edt.GuiActionRunner
import org.assertj.swing.fixture.Containers
import org.assertj.swing.fixture.FrameFixture
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import javax.swing.tree.TreePath


/**
 * Unit tests for [TemplateTree].
 *
 * @see TemplateListEditorTest
 */
object TemplateTreeTest : Spek({
    lateinit var ideaFixture: IdeaTestFixture
    lateinit var frame: FrameFixture

    lateinit var basicTemplates: TemplateList // Not loaded by default, but easy to reuse
    lateinit var tree: TemplateTree


    beforeEachTest {
        ideaFixture = IdeaTestFixtureFactory.getFixtureFactory().createBareFixture()
        ideaFixture.setUp()

        basicTemplates = TemplateList(
            listOf(
                Template("Small", listOf(DummyScheme.from("a"), DummyScheme.from("b"), DummyScheme.from("c"))),
                Template("Clock", listOf(DummyScheme.from("d"), DummyScheme.from("e"))),
                Template("Bite", listOf(DummyScheme.from("f"), DummyScheme.from("g")))
            )
        )
        tree = GuiActionRunner.execute<TemplateTree> { TemplateTree { false } }
        frame = Containers.showInFrame(tree)
    }

    afterEachTest {
        frame.cleanUp()
        ideaFixture.tearDown()
    }


    describe("loadState") {
        it("loads the given templates and schemes into the tree") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }

            val templates = tree.root.children().toList()
            assertThat(templates.map { it.state.name }).containsExactly("Small", "Clock", "Bite")

            val schemes = templates.flatMap { it.children().toList() }
            assertThat(schemes.map { (it.state as Scheme).name }).containsExactly("a", "b", "c", "d", "e", "f", "g")
        }

        it("loads other templates and schemes into the tree") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }

            val otherTemplates = TemplateList(listOf(Template("Deserve"), Template("Charge")))
            GuiActionRunner.execute { tree.loadList(otherTemplates) }

            assertThat(tree.root.children().toList().map { it.state.name }).containsExactly("Deserve", "Charge")
        }

        describe("synchronization") {
            it("synchronizes with external template additions after a reset") {
                GuiActionRunner.execute { tree.loadList(basicTemplates) }

                basicTemplates.templates = listOf(Template("Rail"))
                GuiActionRunner.execute { tree.myModel.reload() }

                assertThat(tree.root.children().toList().map { it.state.name }).containsExactly("Rail")
            }

            it("synchronizes with external scheme additions after a reset") {
                GuiActionRunner.execute { tree.loadList(basicTemplates) }

                basicTemplates.templates[0].schemes = listOf(DummyScheme.from("fear"))
                GuiActionRunner.execute { tree.myModel.reload() }

                assertThat(tree.root.children().toList()[0].children().toList().map { (it.state as Scheme).name })
                    .containsExactly("fear")
            }
        }

        describe("selection") {
            it("selects the first scheme after reload") {
                GuiActionRunner.execute {
                    tree.loadList(TemplateList(listOf(Template("Furnish", listOf(DummyScheme())))))
                }

                assertThat(tree.isRowSelected(1)).isTrue()
            }

            it("selects the first template if it does not have any schemes") {
                GuiActionRunner.execute {
                    tree.loadList(
                        TemplateList(listOf(Template("Flame", emptyList()), Template("Pen", listOf(DummyScheme()))))
                    )
                }

                assertThat(tree.isRowSelected(0)).isTrue()
            }

            it("does nothing if no templates or schemes are loaded") {
                GuiActionRunner.execute { tree.loadList(TemplateList(emptyList())) }

                assertThat(tree.selectedNode).isNull()
            }
        }
    }


    describe("addScheme") {
        beforeEachTest {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
        }


        describe("add template") {
            it("adds the template to the bottom if no node is selected") {
                GuiActionRunner.execute {
                    tree.clearSelection()
                    tree.addScheme(Template(name = "Game"))
                }

                assertThat(tree.isRowSelected(10))
                assertThat(tree.root.children().toList().map { it.state.name })
                    .containsExactly("Small", "Clock", "Bite", "Game")
                assertThat(basicTemplates.templates.map { it.name })
                    .containsExactly("Small", "Clock", "Bite", "Game")
            }

            it("adds the template underneath the currently selected template") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(4)
                    tree.addScheme(Template(name = "Flower"))
                }

                assertThat(tree.isRowSelected(7))
                assertThat(tree.root.children().toList().map { it.state.name })
                    .containsExactly("Small", "Clock", "Flower", "Bite")
                assertThat(basicTemplates.templates.map { it.name })
                    .containsExactly("Small", "Clock", "Flower", "Bite")
            }

            it("adds the template underneath the parent of the currently selected scheme") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(2)
                    tree.addScheme(Template(name = "Agency"))
                }

                assertThat(tree.isRowSelected(4))
                assertThat(tree.root.children().toList().map { it.state.name })
                    .containsExactly("Small", "Agency", "Clock", "Bite")
                assertThat(basicTemplates.templates.map { it.name })
                    .containsExactly("Small", "Agency", "Clock", "Bite")
            }
        }

        describe("add scheme") {
            it("fails if no node is selected") {
                GuiActionRunner.execute {
                    frame.tree().target().clearSelection()

                    assertThatThrownBy { tree.addScheme(DummyScheme()) }
                        .isInstanceOf(IllegalStateException::class.java)
                        .hasMessage("Cannot add non-template to root.")
                }
            }

            it("adds the scheme at the bottom of the selected template") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(7)
                    tree.addScheme(DummyScheme.from("z"))
                }

                assertThat(tree.isRowSelected(4))
                assertThat(tree.root.children().toList()[2].children().toList().map { (it.state as DummyScheme).name })
                    .containsExactly("f", "g", "z")
                assertThat(basicTemplates.templates[2].schemes.map { it.name })
                    .containsExactly("f", "g", "z")
            }

            it("adds the scheme underneath the currently selected scheme") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(1)
                    tree.addScheme(DummyScheme.from("z"))
                }

                assertThat(tree.isRowSelected(2))
                assertThat(tree.root.children().toList()[0].children().toList().map { (it.state as DummyScheme).name })
                    .containsExactly("a", "z", "b", "c")
                assertThat(basicTemplates.templates[0].schemes.map { it.name })
                    .containsExactly("a", "z", "b", "c")
            }
        }
    }

    describe("removeNode") {
        beforeEachTest {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
        }


        describe("remove template") {
            it("removes the template from the tree") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(0)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat(tree.root.children().toList().map { it.state.name }).containsExactly("Clock", "Bite")
            }

            it("removes the template from the model") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(4)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat(basicTemplates.templates.map { it.name }).containsExactly("Small", "Bite")
            }

            it("selects nothing if the last template is removed") {
                GuiActionRunner.execute {
                    repeat(3) {
                        tree.setSelectionRow(0)
                        tree.removeNode(tree.selectedNode!!)
                    }
                }

                assertThat(tree.selectedNode).isNull()
            }

            it("selects the next template if a non-bottom template is removed") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(4)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat((tree.selectedNode?.state as Template).name).isEqualTo("Bite")
                assertThat(tree.isRowSelected(4))
            }

            it("selects the new bottom template if the bottom template is removed") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(7)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat((tree.selectedNode?.state as Template).name).isEqualTo("Clock")
                assertThat(tree.isRowSelected(4))
            }
        }

        describe("remove scheme") {
            it("removes the scheme from the tree") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(2)
                    tree.removeNode(tree.selectedNode!!)
                }

                val schemes = tree.root.children().toList()[0].children().toList()
                assertThat(schemes.map { (it.state as DummyScheme).name }).containsExactly("a", "c")
            }

            it("removes the scheme from the model") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(6)
                    tree.removeNode(tree.selectedNode!!)
                }

                val schemes = tree.root.children().toList()[1].children().toList()
                assertThat(schemes.map { (it.state as DummyScheme).name }).containsExactly("d")
            }

            it("selects the template if its last scheme is removed") {
                GuiActionRunner.execute {
                    repeat(2) {
                        tree.setSelectionRow(5)
                        tree.removeNode(tree.selectedNode!!)
                    }
                }

                assertThat((tree.selectedNode?.state as Template).name).isEqualTo("Clock")
                assertThat(tree.isRowSelected(4))
            }

            it("selects the next scheme if a non-bottom scheme is removed") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(1)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat((tree.selectedNode?.state as Scheme).name).isEqualTo("b")
                assertThat(tree.isRowSelected(1))
            }

            it("selects the new bottom scheme if the bottom scheme is removed") {
                GuiActionRunner.execute {
                    frame.tree().target().setSelectionRow(9)
                    tree.removeNode(tree.selectedNode!!)
                }

                assertThat((tree.selectedNode?.state as Scheme).name).isEqualTo("f")
                assertThat(tree.isRowSelected(8))
            }
        }
    }

    describe("moveNodeDownBy") {
        beforeEachTest {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
        }


        describe("templates") {
            it("fails if the template would go too far up") {
                GuiActionRunner.execute { tree.setSelectionRow(4) }

                assertThatThrownBy { tree.moveNodeDownBy(tree.selectedNode!!, -2) }.isNotNull()
            }

            it("fails if the template would go too far down") {
                GuiActionRunner.execute { tree.setSelectionRow(4) }

                assertThatThrownBy { tree.moveNodeDownBy(tree.selectedNode!!, 4) }.isNotNull()
            }

            it("moves the template up by the given number of positions") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(4)
                    tree.moveNodeDownBy(tree.selectedNode!!, 1)
                }

                assertThat(tree.root.children().toList().map { it.state.name })
                    .containsExactly("Small", "Bite", "Clock")
            }

            it("moves the template down by the given number of positions") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(7)
                    tree.moveNodeDownBy(tree.selectedNode!!, -2)
                }

                assertThat(tree.root.children().toList().map { it.state.name })
                    .containsExactly("Bite", "Small", "Clock")
            }

            it("selects the moved template") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(0)
                    tree.moveNodeDownBy(tree.selectedNode!!, 2)
                }

                assertThat((tree.selectedNode?.state as Template).name).isEqualTo("Small")
                assertThat(tree.isRowSelected(6)).isTrue()
            }

            it("modifies the original list of templates") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(4)
                    tree.moveNodeDownBy(tree.selectedNode!!, 1)
                }

                assertThat(basicTemplates.templates.map { it.name }).containsExactly("Small", "Bite", "Clock")
            }
        }

        describe("schemes") {
            it("fails if the scheme would go too far up") {
                GuiActionRunner.execute { tree.setSelectionRow(2) }

                assertThatThrownBy { tree.moveNodeDownBy(tree.selectedNode!!, -2) }.isNotNull()
            }

            it("fails if the scheme would go too far down") {
                GuiActionRunner.execute { tree.setSelectionRow(5) }

                assertThatThrownBy { tree.moveNodeDownBy(tree.selectedNode!!, 2) }.isNotNull()
            }

            it("moves the scheme up by the given number of positions") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(3)
                    tree.moveNodeDownBy(tree.selectedNode!!, -2)
                }

                val schemes = tree.root.children().toList()[0].children().toList()
                assertThat(schemes.map { (it.state as DummyScheme).name })
                    .containsExactly("c", "a", "b")
            }

            it("moves the scheme down by the given number of positions") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(5)
                    tree.moveNodeDownBy(tree.selectedNode!!, 1)
                }

                val schemes = tree.root.children().toList()[1].children().toList()
                assertThat(schemes.map { (it.state as DummyScheme).name }).containsExactly("e", "d")
            }

            it("selects the moved scheme") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(8)
                    tree.moveNodeDownBy(tree.selectedNode!!, 1)
                }

                assertThat((tree.selectedNode?.state as DummyScheme).name).isEqualTo("f")
                assertThat(tree.isRowSelected(9)).isTrue()
            }

            it("modifies the original list of schemes in the template") {
                GuiActionRunner.execute {
                    tree.setSelectionRow(1)
                    tree.moveNodeDownBy(tree.selectedNode!!, 2)
                }

                assertThat(basicTemplates.templates[0].schemes.map { (it as DummyScheme).name })
                    .containsExactly("b", "c", "a")
            }
        }

        it("notifies observers of an insertion and of a removal") {
            var isInvoked = 0
            tree.myModel.addTreeModelListener(SimpleTreeModelListener { isInvoked++ })

            GuiActionRunner.execute { tree.moveNodeDownBy(tree.selectedNode!!, 1) }

            assertThat(isInvoked).isEqualTo(2)
        }

        it("does nothing if the number of positions is 0") {
            var isInvoked = 0
            tree.myModel.addTreeModelListener(SimpleTreeModelListener { isInvoked++ })

            GuiActionRunner.execute { tree.moveNodeDownBy(tree.selectedNode!!, 0) }

            assertThat(isInvoked).isZero()
        }
    }

    describe("selectTemplate") {
        it("does nothing if no template with the specified UUID can be found") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
            val initialSelection = tree.selectedNode?.state

            GuiActionRunner.execute { tree.selectTemplate("dae4e446-56de-40ca-8415-5bff1f47f2f4") }

            assertThat(tree.selectedNode?.state).isEqualTo(initialSelection)
        }

        it("does nothing if the UUID belongs to a scheme in the tree") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
            val initialSelection = tree.selectedNode?.state

            GuiActionRunner.execute { tree.selectTemplate(basicTemplates.templates[1].schemes[0].uuid) }

            assertThat(tree.selectedNode?.state).isEqualTo(initialSelection)
        }

        it("selects the template with the given UUID") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }

            GuiActionRunner.execute { tree.selectTemplate(basicTemplates.templates[1].uuid) }

            assertThat(tree.selectedNode?.state).isEqualTo(basicTemplates.templates[1])
        }
    }


    describe("getSelectedNode") {
        beforeEachTest {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }
        }


        it("returns null if nothing is selected") {
            GuiActionRunner.execute { tree.clearSelection() }

            assertThat(tree.selectedNode).isNull()
        }

        it("returns null if the root is selected") {
            GuiActionRunner.execute { tree.selectionPath = TreePath(tree.myModel.root) }

            assertThat(tree.selectedNode).isNull()
        }

        it("returns the selected node if the node is a template") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }

            GuiActionRunner.execute { tree.setSelectionRow(4) }

            assertThat((tree.selectedNode?.state as Scheme).name).isEqualTo("Clock")
        }

        it("returns the selected node if the node is a scheme") {
            GuiActionRunner.execute { tree.loadList(basicTemplates) }

            GuiActionRunner.execute { tree.setSelectionRow(8) }

            assertThat((tree.selectedNode?.state as Scheme).name).isEqualTo("f")
        }
    }
})
