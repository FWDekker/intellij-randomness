package com.fwdekker.randomness.template

import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.testhelpers.DummyScheme
import com.fwdekker.randomness.testhelpers.beEmptyIntArray
import com.fwdekker.randomness.testhelpers.beforeNonContainer
import com.fwdekker.randomness.testhelpers.matchBundle
import com.fwdekker.randomness.testhelpers.shouldContainExactly
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.Row2
import io.kotest.data.Row3
import io.kotest.data.row
import io.kotest.datatest.withData
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener


/**
 * Unit tests for [TemplateJTreeModel].
 */
object TemplateJTreeModelTest : FunSpec({
    lateinit var list: TemplateList
    lateinit var model: TemplateJTreeModel

    fun List<Scheme>.names() = this.map { it.name }


    beforeNonContainer {
        list = TemplateList(
            mutableListOf(
                Template(
                    "Template0", // row 0
                    mutableListOf(
                        DummyScheme("Scheme0"), // row 1
                        DummyScheme("Scheme1"), // row 2
                    ),
                ),
                Template(
                    "Template1", // row 3
                    mutableListOf(DummyScheme("Scheme2")), // row 4
                ),
                Template(
                    "Template2", // row 5
                    mutableListOf(),
                ),
            )
        )
        model = TemplateJTreeModel(list)
    }


    context("addRow") {
        test("throws an error") {
            shouldThrow<UnsupportedOperationException> { model.addRow() }
        }
    }

    context("removeRow") {
        test("throws an error") {
            shouldThrow<UnsupportedOperationException> { model.removeRow(4) }
        }
    }


    context("insertNode") {
        test("throws an error if the parent is not in the tree") {
            val parent = StateNode(Template())
            val child = StateNode(DummyScheme())

            shouldThrow<IllegalArgumentException> { model.insertNode(parent, child) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        test("throws an error if the child is not a valid child for the parent") {
            val parent = model.root
            val child = StateNode(DummyScheme())

            shouldThrow<IllegalArgumentException> { model.insertNode(parent, child) }
                .message should matchBundle("template_list.error.wrong_child_type")
        }

        test("throws an error when a node is inserted at a negative index") {
            shouldThrow<IndexOutOfBoundsException> { model.insertNode(model.root, StateNode(Template()), index = -2) }
        }

        test("throws an error if a node is inserted at a too-high index") {
            shouldThrow<IndexOutOfBoundsException> { model.insertNode(model.root, StateNode(Template()), index = 14) }
        }


        test("inserts a node at a specific index") {
            val node = StateNode(Template("Template3"))

            model.insertNode(model.root, node, index = 1)

            model.root.children shouldHaveSize 4
            model.root.children[1] shouldBe node
            list.templates.names() shouldContainExactly listOf("Template0", "Template3", "Template1", "Template2")
        }

        test("inserts a node at the last index of the parent") {
            val node = StateNode(Template("Template3"))

            model.insertNode(model.root, node, index = 3)

            model.root.children shouldHaveSize 4
            model.root.children[3] shouldBe node
            list.templates.names() shouldContainExactly listOf("Template0", "Template1", "Template2", "Template3")
        }


        test("informs listeners of the insertion") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val scheme = StateNode(DummyScheme())
            model.insertNode(model.root.children[0], scheme)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root.children[0]
            lastEvent!!.childIndices shouldContainExactly arrayOf(2)
            lastEvent!!.children shouldContainExactly arrayOf(scheme)
        }

        test("informs listeners of a node insertion if the root's not-only child was inserted") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val node = StateNode(Template("Template3"))
            model.insertNode(model.root, node, index = 1)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root
            lastEvent!!.childIndices shouldContainExactly arrayOf(1)
            lastEvent!!.children shouldContainExactly arrayOf(node)
        }

        test("informs listeners of a structure change if the root's now-only child was inserted") {
            list.templates.clear()
            model.fireNodeStructureChanged()

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val node = StateNode(Template("Template3"))
            model.insertNode(model.root, node)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root
            lastEvent!!.childIndices should beEmptyIntArray()
            lastEvent!!.children should beNull()
        }
    }

    context("insertNodeAfter") {
        test("throws an error if the parent is not in the tree") {
            val template = Template(schemes = mutableListOf(DummyScheme("after")))

            val parent = StateNode(template)
            val after = StateNode(template.schemes[0])
            val child = StateNode(DummyScheme("Child"))

            shouldThrow<IllegalArgumentException> { model.insertNodeAfter(parent, after, child) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        test("throws an error if the parent cannot have children") {
            val parent = StateNode(list.templates[0].schemes[0])
            val after = StateNode(DummyScheme())
            val child = StateNode(DummyScheme())

            shouldThrow<IllegalStateException> { model.insertNodeAfter(parent, after, child) }
                .message should matchBundle("template_list.error.infertile_parent")
        }

        test("throws an error if the node to insert after is not in the parent") {
            val parent = StateNode(list.templates[1])
            val after = StateNode(list.templates[0].schemes[0])
            val child = StateNode(DummyScheme())

            shouldThrow<IllegalArgumentException> { model.insertNodeAfter(parent, after, child) }
                .message should matchBundle("template_list.error.wrong_parent")
        }


        test("inserts the node after the given node") {
            val parent = StateNode(list.templates[0])
            val after = StateNode(list.templates[0].schemes[1])
            val child = StateNode(DummyScheme("Child"))

            model.insertNodeAfter(parent, after, child)

            parent.children[2] shouldBe child
        }
    }

    context("removeNode") {
        test("throws an error if the given node is not contained in the model") {
            shouldThrow<IllegalArgumentException> { model.removeNode(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        test("throws an error if the given node is the root of the model") {
            shouldThrow<IllegalArgumentException> { model.removeNode(model.root) }
                .message should matchBundle("template_list.error.cannot_remove_root")
        }


        test("removes a node from the model and the underlying list") {
            val node = model.root.children[1]

            model.removeNode(node)

            model.root.children shouldHaveSize 2
            model.root.children shouldNotContain node
            list.templates shouldHaveSize 2
            list.templates shouldNotContain node.state
        }

        test("informs listeners of the removal") {
            val node = model.root.children[2]

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })
            model.removeNode(node)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root
            lastEvent!!.childIndices shouldContainExactly arrayOf(2)
            lastEvent!!.children shouldContainExactly arrayOf(node)
        }
    }

    context("exchangeRows") {
        test("cannot swap out-of-bounds indices") {
            shouldThrow<IllegalArgumentException> { model.exchangeRows(3, 495) }
                .message should matchBundle("template_list.error.cannot_swap_rows")
        }

        context("templates") {
            test("swaps a template with the previous template") {
                model.exchangeRows(5, 3)

                list.templates.names() shouldContainExactly listOf("Template0", "Template2", "Template1")
            }

            test("swaps a template with the next template") {
                model.exchangeRows(0, 3)

                list.templates.names() shouldContainExactly listOf("Template1", "Template0", "Template2")
            }
        }

        context("schemes") {
            test("swaps a scheme with the previous scheme under the same parent") {
                model.exchangeRows(1, 2)

                list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }

            test("swaps a scheme with the next scheme under the same parent") {
                model.exchangeRows(2, 1)

                list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }

            test("'swaps' a scheme with its parent, making it the last child of the parent's previous sibling") {
                model.exchangeRows(4, 3)

                list.templates[0].schemes.names() shouldContainExactly listOf("Scheme0", "Scheme1", "Scheme2")
                list.templates[1].schemes should beEmpty()
            }

            test("moves a scheme to its parent's next sibling, making it that sibling's first child") {
                model.exchangeRows(4, 5)

                list.templates[1].schemes should beEmpty()
                list.templates[2].schemes.names() shouldContainExactly listOf("Scheme2")
            }
        }
    }

    context("canExchangeRows") {
        withData(
            mapOf(
                "old node cannot be found" to row(-2, 2, false),
                "new node cannot be found" to row(1, -4, false),
                "old is template, new is non-template" to row(0, 2, false),
                "old is non-template, new is first template" to row(1, 0, false),
                "old is non-template, new is non-first template" to row(4, 3, true),
                "both are templates" to row(0, 5, true),
                "both are schemes" to row(1, 4, true),
            )
        ) { (oldIndex, newIndex, expected) -> model.canExchangeRows(oldIndex, newIndex) shouldBe expected }
    }


    context("isLeaf") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.isLeaf("not a node") }
                .message should matchBundle("template_list.error.unknown_node_type")
        }

        test("throws an error if the given node is not contained in the model") {
            shouldThrow<IllegalArgumentException> { model.isLeaf(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        withData(
            mapOf(
                "cannot have children" to row({ model.root.children[0].children[1] }, true),
                "can have children, but has none" to row({ model.root.children[2] }, true),
                "can have children and has children" to row({ model.root.children[0] }, false),
            )
        ) { (state, expected) -> model.isLeaf(state()) shouldBe expected }
    }

    context("getChild") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.getChild("not a node", index = 0) }
                .message should matchBundle("template_list.error.unknown_node_type")
        }

        test("throws an error if the given node is not contained in the model") {
            shouldThrow<IllegalArgumentException> { model.getChild(StateNode(DummyScheme()), 4) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        test("throws an error if the given node cannot have children") {
            shouldThrow<IllegalStateException> { model.getChild(model.root.children[0].children[1], index = 0) }
                .message should matchBundle("template_list.error.infertile_parent")
        }

        test("throws an error if there is no child at the given index") {
            shouldThrow<IndexOutOfBoundsException> { model.getChild(model.root.children[1], index = 2) }
        }

        test("returns the child at the given index") {
            model.getChild(model.root.children[1], index = 0).state shouldBe list.templates[1].schemes[0]
        }
    }

    context("getChildCount") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.getChildCount("not a node") }
                .message should matchBundle("template_list.error.unknown_node_type")
        }

        test("throws an error if the given node is not contained in the model") {
            shouldThrow<IllegalArgumentException> { model.getChildCount(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        withData(
            mapOf(
                "cannot have children" to row({ model.root.children[0].children[1] }, 0),
                "can have children, but has none" to row({ model.root.children[2] }, 0),
                "can have children and has children" to row({ model.root.children[0] }, 2),
            )
        ) { (state, expected) -> model.getChildCount(state()) shouldBe expected }
    }

    context("getIndexOfChild") {
        withData(
            mapOf<String, Row3<() -> Any?, () -> Any?, Int>>(
                "parent is not a StateNode" to
                    row({ "parent" }, { model.root.children[0].children[0] }, -1),
                "child is not a StateNode" to
                    row({ model.root.children[1] }, { "child" }, -1),
                "parent is null" to
                    row({ null }, { model.root.children[1].children[0] }, -1),
                "child is null" to
                    row({ model.root.children[1] }, { null }, -1),
                "parent is not child's parent" to
                    row({ model.root.children[2] }, { model.root.children[0].children[1] }, -1),
                "parent is child's parent" to
                    row({ model.root.children[1] }, { model.root.children[1].children[0] }, 0),
                "parent matches by UUID only" to
                    row(
                        { model.root.children[0] },
                        { StateNode(list.templates[0].schemes[1].deepCopy(retainUuid = true)) },
                        1,
                    ),
            )
        ) { (parent, child, expected) -> model.getIndexOfChild(parent(), child()) shouldBe expected }
    }

    context("getParentOf") {
        test("throws an error if the node is not contained in this model") {
            shouldThrow<IllegalArgumentException> { model.getParentOf(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        withData(
            mapOf<String, Row2<() -> StateNode, () -> StateNode?>>(
                "parent of root" to row({ model.root }, { null }),
                "parent of template" to row({ model.root.children[0] }, { model.root }),
                "parent of scheme" to row({ model.root.children[1].children[0] }, { model.root.children[1] }),
            )
        ) { (node, expected) -> model.getParentOf(node()) shouldBe expected() }
    }

    context("getPathToRoot") {
        test("throws an error if the node is not contained in this model") {
            shouldThrow<IllegalArgumentException> { model.getPathToRoot(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.node_not_in_tree")
        }

        withData(
            mapOf(
                "path to root" to
                    row({ model.root }, { arrayOf(model.root) }),
                "path to template" to
                    row({ model.root.children[0] }, { arrayOf(model.root, model.root.children[0]) }),
                "path to scheme" to
                    row(
                        { model.root.children[1].children[0] },
                        { arrayOf(model.root, model.root.children[1], model.root.children[1].children[0]) },
                    ),
            )
        ) { (node, expected) -> model.getPathToRoot(node()).path shouldBe expected() }
    }


    context("fire methods") {
        var nodesChangedInvoked = 0
        var nodesInsertedInvoked = 0
        var nodesRemovedInvoked = 0
        var structureChangedInvoked = 0
        var totalInvoked = 0
        var lastEvent: TreeModelEvent? = null


        beforeNonContainer {
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


        context("fireNodeChanged") {
            test("does nothing if the given node is null") {
                model.fireNodeChanged(null)

                nodesChangedInvoked shouldBe 0
                totalInvoked - nodesChangedInvoked shouldBe 0
            }

            test("invokes the listener with an event without children if the current root is provided") {
                model.fireNodeChanged(model.root)

                nodesChangedInvoked shouldBe 1
                totalInvoked - nodesChangedInvoked shouldBe 0
                lastEvent!!.treePath.lastPathComponent shouldBe model.root
            }

            test("indicates the parent and child index when a non-root node is changed") {
                val node = model.root.children[0]

                model.fireNodeChanged(node)

                nodesChangedInvoked shouldBe 1
                totalInvoked - nodesChangedInvoked shouldBe 0
                lastEvent!!.treePath.lastPathComponent shouldBe model.root
                lastEvent!!.childIndices shouldContainExactly arrayOf(0)
                lastEvent!!.children shouldContainExactly arrayOf(node)
            }
        }

        context("fireNodeInserted") {
            test("throws an error if a template list is given") {
                val node = StateNode(DummyScheme())

                shouldThrow<IllegalArgumentException> { model.fireNodeInserted(model.root, node, 688) }
                    .message should matchBundle("template_list.error.cannot_insert_root")
            }

            test("does nothing if the given node is null") {
                model.fireNodeInserted(null, StateNode(DummyScheme()), 227)

                totalInvoked shouldBe 0
            }

            test("fires an event with the given parent, node, and index") {
                val parent = model.root.children[0]
                val newNode = StateNode(DummyScheme())

                model.fireNodeInserted(newNode, parent, 1)

                nodesInsertedInvoked shouldBe 1
                totalInvoked - nodesInsertedInvoked shouldBe 0
                lastEvent!!.treePath.lastPathComponent shouldBe parent
                lastEvent!!.childIndices shouldContainExactly arrayOf(1)
                lastEvent!!.children shouldContainExactly arrayOf(newNode)
            }
        }

        context("fireNodeRemoved") {
            test("does nothing if the given node is null") {
                model.fireNodeRemoved(null, StateNode(DummyScheme()), 247)

                totalInvoked shouldBe 0
            }

            test("throws an error if a template list is given") {
                val node = StateNode(DummyScheme())

                shouldThrow<IllegalArgumentException> { model.fireNodeRemoved(model.root, node, 888) }
                    .message should matchBundle("template_list.error.cannot_remove_root")
            }

            test("fires an event with the given parent, node, and index") {
                val parent = model.root.children[0]
                val oldNode = parent.children[0]

                model.fireNodeRemoved(oldNode, parent, 0)

                nodesRemovedInvoked shouldBe 1
                totalInvoked - nodesRemovedInvoked shouldBe 0
                lastEvent!!.treePath.lastPathComponent shouldBe parent
                lastEvent!!.childIndices shouldContainExactly arrayOf(0)
                lastEvent!!.children shouldContainExactly arrayOf(oldNode)
            }
        }

        context("fireNodeStructureChanged") {
            test("fires an event with the given node") {
                val node = model.root.children[0]

                model.fireNodeStructureChanged(node)

                structureChangedInvoked shouldBe 1
                totalInvoked - structureChangedInvoked shouldBe 0
                lastEvent!!.treePath.lastPathComponent shouldBe node
                lastEvent!!.childIndices should beEmptyIntArray()
                lastEvent!!.children should beNull()
            }
        }
    }


    context("valueForPathChanged") {
        test("throws an error") {
            shouldThrow<UnsupportedOperationException> {
                model.valueForPathChanged(model.getPathToRoot(model.root), "new-value")
            }
        }
    }

    context("addTreeModelListener") {
        test("invokes the added listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.fireNodeStructureChanged()

            invoked shouldBe 1
        }
    }

    context("removeTreeModelListener") {
        test("no longer invokes the removed listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.fireNodeStructureChanged()

            model.removeTreeModelListener(listener)
            model.fireNodeStructureChanged()

            invoked shouldBe 1
        }
    }
})

/**
 * Unit tests for [StateNode].
 */
object StateNodeTest : FunSpec({
    context("canHaveChildren") {
        withData(
            mapOf(
                "TemplateList" to row(TemplateList(mutableListOf()), true),
                "Template" to row(Template(), true),
                "DummyScheme" to row(DummyScheme(), false),
            )
        ) { (state, expected) -> StateNode(state).canHaveChildren shouldBe expected }
    }

    context("children") {
        context("get") {
            test("returns the templates of a template list") {
                val templates = mutableListOf(Template("Template0"), Template("Template2"))

                val node = StateNode(TemplateList(templates))

                node.children.map { it.state } shouldContainExactly templates
            }

            test("returns the schemes of a template") {
                val schemes = mutableListOf<Scheme>(DummyScheme(), DummyScheme())

                val node = StateNode(Template(schemes = schemes))

                node.children.map { it.state } shouldContainExactly schemes
            }

            test("throws an error for a non-template scheme") {
                shouldThrow<IllegalStateException> { StateNode(DummyScheme()).children }
                    .message should matchBundle("template_list.error.infertile_parent")
            }
        }

        context("set") {
            test("modifies the templates of a template list") {
                val list = TemplateList(mutableListOf(Template("Template0"), Template("Template1")))

                val newTemplate = Template("NewTemplate")
                StateNode(list).children = listOf(StateNode(newTemplate))

                list.templates shouldContainExactly listOf(newTemplate)
            }

            test("modifies the schemes of a template") {
                val template = Template(schemes = mutableListOf(DummyScheme(), DummyScheme()))

                val newScheme = DummyScheme()
                StateNode(template).children = listOf(StateNode(newScheme))

                template.schemes shouldContainExactly listOf(newScheme)
            }

            test("throws an error for a non-template scheme") {
                shouldThrow<IllegalStateException> { run { StateNode(DummyScheme()).children = emptyList() } }
                    .message should matchBundle("template_list.error.infertile_parent")
            }
        }
    }

    context("descendants") {
        test("returns the templates and schemes of a template list in depth-first order") {
            val templates = mutableListOf(
                Template("Template0", mutableListOf(DummyScheme("Scheme0"), DummyScheme("Scheme1"))),
                Template("Template1", mutableListOf(DummyScheme("Scheme1"))),
            )

            val children = StateNode(TemplateList(templates)).descendants

            children.map { (it.state as Scheme).name } shouldContainExactly
                listOf("Template0", "Scheme0", "Scheme1", "Template1", "Scheme1")
        }

        test("returns the schemes of a template") {
            val schemes = mutableListOf<Scheme>(DummyScheme("Scheme0"), DummyScheme("Scheme1"))

            val children = StateNode(Template(schemes = schemes)).descendants

            children.map { (it.state as Scheme).name } shouldContainExactly schemes.map { it.name }
        }

        test("returns an empty list if the node cannot have children") {
            StateNode(DummyScheme()).descendants should beEmpty()
        }
    }


    context("canHaveChild") {
        withData(
            mapOf(
                "TemplateList/TemplateList" to row(TemplateList(mutableListOf()), TemplateList(mutableListOf()), false),
                "TemplateList/Template" to row(TemplateList(mutableListOf()), Template(), true),
                "TemplateList/Scheme" to row(TemplateList(mutableListOf()), DummyScheme(), false),
                "Template/TemplateList" to row(Template(), TemplateList(mutableListOf()), false),
                "Template/Template" to row(Template(), Template(), false),
                "Template/Scheme" to row(Template(), DummyScheme(), true),
                "Scheme/TemplateList" to row(DummyScheme(), TemplateList(mutableListOf()), false),
                "Scheme/Template" to row(DummyScheme(), Template(), false),
                "Scheme/Scheme" to row(DummyScheme(), DummyScheme(), false),
            )
        ) { (parent, child, expected) -> StateNode(parent).canHaveChild(StateNode(child)) shouldBe expected }
    }


    context("equals") {
        test("does not equal an object of another class") {
            StateNode(DummyScheme()) shouldNotBe 299
        }

        test("equals itself") {
            val node = StateNode(DummyScheme())

            node shouldBe node
        }

        test("equals a node of a copy with the same UUID") {
            val scheme = DummyScheme()

            StateNode(scheme) shouldBe StateNode(scheme.deepCopy(retainUuid = true))
        }

        test("does not equal a node of a copy with a different UUID") {
            val scheme = DummyScheme()

            StateNode(scheme) shouldNotBe StateNode(scheme.deepCopy(retainUuid = false))
        }

        test("does not equal a different object with a different UUID") {
            StateNode(DummyScheme()) shouldNotBe StateNode(DummyScheme())
        }
    }

    context("hashCode") {
        test("is deterministic") {
            val node = StateNode(DummyScheme())

            node.hashCode() shouldBe node.hashCode()
        }

        test("equals the hashcode of an identical object") {
            val scheme = DummyScheme()

            val node1 = StateNode(scheme)
            val node2 = StateNode(scheme)

            node1.hashCode() shouldBe node2.hashCode()
        }

        test("equals the hashcode of a deep copy with the same UUID") {
            val scheme = DummyScheme()

            val node1 = StateNode(scheme)
            val node2 = StateNode(scheme.deepCopy(retainUuid = true))

            node1.hashCode() shouldBe node2.hashCode()
        }

        test("does not equal the hashcode of a deep copy with the same UUID") {
            val scheme = DummyScheme()

            val node1 = StateNode(scheme)
            val node2 = StateNode(scheme.deepCopy(retainUuid = false))

            node1.hashCode() shouldNotBe node2.hashCode()
        }

        test("does not equal the hashcode of a different object") {
            val node1 = StateNode(DummyScheme())
            val node2 = StateNode(DummyScheme())

            node1.hashCode() shouldNotBe node2.hashCode()
        }
    }
})
