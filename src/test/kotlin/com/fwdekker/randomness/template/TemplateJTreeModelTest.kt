package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.State
import com.fwdekker.randomness.beEmptyIntArray
import com.fwdekker.randomness.matchBundle
import com.fwdekker.randomness.shouldContainExactly
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.collections.beEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.collections.shouldNotContain
import io.kotest.matchers.nulls.beNull
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener


/**
 * Unit tests for [TemplateJTreeModel].
 */
object TemplateJTreeModelTest : FunSpec({
    lateinit var model: TemplateJTreeModel

    fun List<Scheme>.names() = this.map { it.name }


    beforeEach {
        model = TemplateJTreeModel(
            TemplateList(
                mutableListOf(
                    Template(
                        // row 0
                        "Template0",
                        mutableListOf(
                            DummyScheme("Scheme0"), // row 1
                            DummyScheme("Scheme1"), // row 2
                        ),
                    ),
                    Template(
                        // row 3
                        "Template1",
                        mutableListOf(DummyScheme("Scheme2")), // row 4
                    ),
                    Template("Template2", mutableListOf()), // row 5
                )
            )
        )
    }


    test("rowToNode (default implementation)") {
        forAll(
            table(
                headers("description", "row", "node"),
                row("negative index", -2, null),
                row("too-high index", 241, null),
                row("0 is first template", 0, model.root.children[0]),
                row("1 is first leaf", 1, model.root.children[0].children[0]),
                row("row number for template includes siblings' schemes", 5, model.root.children[2]),
                row("row number for scheme includes aunts'/uncles' schemes", 4, model.root.children[1].children[0]),
            )
        ) { _, row, node -> model.rowToNode(row) shouldBe node }
    }

    test("nodeToRow (default implementation)") {
        forAll(
            table(
                headers("description", "node", "row"),
                row("null", null, -1),
                row("unknown node", StateNode(DummyScheme()), -1),
                row("first template is 0", model.root.children[0], 0),
                row("first scheme is 1", model.root.children[0].children[0], 1),
                row("row number for template includes siblings' schemes", model.root.children[2], 5),
                row("row number for scheme includes aunts'/uncles' schemes", model.root.children[1].children[0], 4),
            )
        ) { _, node, row -> model.nodeToRow(node) shouldBe row }
    }


    test("reload") {
        test("informs listeners that the root has been reloaded") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val oldList = model.list
            model.reload()

            model.list shouldBeSameInstanceAs oldList
            lastEvent!!.treePath.lastPathComponent shouldBe model.root
            lastEvent!!.childIndices should beEmptyIntArray()
            lastEvent!!.children should beNull()
        }

        test("informs listeners that a different root has been loaded") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val newList = TemplateList(mutableListOf())
            model.reload(newList)

            model.list shouldBeSameInstanceAs newList
            lastEvent!!.treePath.lastPathComponent shouldBe StateNode(newList)
            lastEvent!!.childIndices should beEmptyIntArray()
            lastEvent!!.children should beNull()
        }
    }


    test("addRow") {
        test("throws an error") {
            shouldThrow<NotImplementedError> { model.addRow() }
                .message should matchBundle("template_list.error.add_empty_row")
        }
    }

    test("removeRow") {
        test("throws an error") {
            shouldThrow<NotImplementedError> { model.removeRow(4) }
                .message should matchBundle("template_list.error.remove_row_by_index")
        }
    }

    test("exchangeRows") {
        test("templates") {
            test("swaps a template with the previous template") {
                model.exchangeRows(5, 3)

                model.list.templates.names() shouldContainExactly listOf("Template0", "Template2", "Template1")
            }

            test("swaps a template with the next template") {
                model.exchangeRows(0, 3)

                model.list.templates.names() shouldContainExactly listOf("Template1", "Template0", "Template2")
            }

            test("swaps a template with the second-next template") {
                model.exchangeRows(0, 5)

                model.list.templates.names() shouldContainExactly listOf("Template2", "Template0", "Template1")
            }
        }

        test("schemes") {
            test("swaps a scheme with the previous scheme under the same parent") {
                model.exchangeRows(1, 2)

                model.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }

            test("swaps a scheme with the next scheme under the same parent") {
                model.exchangeRows(2, 1)

                model.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme1", "Scheme0")
            }

            test("'swaps' a scheme with its parent, making it the last child of the parent's previous sibling") {
                model.exchangeRows(4, 3)

                model.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme0", "Scheme1", "Scheme2")
                model.list.templates[1].schemes should beEmpty()
            }

            test("moves a scheme to its parent's next sibling, making it that sibling's first child") {
                model.exchangeRows(4, 5)

                model.list.templates[1].schemes should beEmpty()
                model.list.templates[2].schemes.names() shouldContainExactly listOf("Scheme2")
            }

            test("moves a scheme to a scheme in another template") {
                model.exchangeRows(1, 4)

                model.list.templates[0].schemes.names() shouldContainExactly listOf("Scheme2", "Scheme1")
                model.list.templates[1].schemes.names() shouldContainExactly listOf("Scheme0")
            }
        }
    }

    test("canExchangeRows") {
        forAll(
            table(
                headers("description", "oldIndex", "newIndex", "expected"),
                row("old node cannot be found", -2, 2, false),
                row("new node cannot be found", 1, -4, false),
                row("old is template, new is non-template", 0, 2, false),
                row("old is non-template, new is first template", 1, 0, false),
                row("old is non-template, new is non-first template", 4, 3, true),
                row("both are templates", 0, 5, true),
                row("both are schemes", 1, 4, true),
            )
        ) { _, oldIndex, newIndex, expected -> model.canExchangeRows(oldIndex, newIndex) shouldBe expected }
    }


    test("isLeaf") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.isLeaf("not a node") }
                .message should matchBundle("template_list.error.must_be_state_node")
        }

        test("parameterized") {
            forAll(
                table(
                    headers("description", "state", "expected"),
                    row("cannot have children", model.root.children[0].children[1], true),
                    row("can have children, but has none", model.root.children[2], true),
                    row("can have children and has children", model.root.children[0], false),
                )
            ) { _, state, expected -> model.isLeaf(state) shouldBe expected }
        }
    }

    test("getChild") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.getChild("not a node", index = 0) }
                .message should matchBundle("template_list.error.must_be_state_node")
        }

        test("throws an error if the given node cannot have children") {
            shouldThrow<IllegalArgumentException> { model.getChild(model.root.children[0].children[1], index = 0) }
                .message should matchBundle("template_list.error.child_of_infertile_parent")
        }

        test("throws an error if there is no child at the given index") {
            shouldThrow<IndexOutOfBoundsException> { model.getChild(model.root.children[1], index = 2) }
        }

        test("returns the child at the given index") {
            model.getChild(model.root.children[1], index = 0).state shouldBe model.list.templates[1].schemes[0]
        }
    }

    test("getChildCount") {
        test("throws an error if the given node is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.getChildCount("not a node") }
                .message should matchBundle("template_list.error.must_be_state_node")
        }

        test("parameterized") {
            forAll(
                table(
                    headers("description", "state", "expected"),
                    row("cannot have children", model.root.children[0].children[1], 0),
                    row("can have children, but has none", model.root.children[2], 0),
                    row("can have children and has children", model.root.children[0], 2),
                )
            ) { _, state, expected -> model.getChildCount(state) shouldBe expected }
        }
    }

    test("getIndexOfChild") {
        test("throws an error if the parent is not a StateNode") {
            val child = model.root.children[0].children[1]

            shouldThrow<IllegalArgumentException> { model.getIndexOfChild("not a node", child) }
                .message should matchBundle("template_list.error.must_be_state_node", "Parent")
        }

        test("throws an error if the child is not a StateNode") {
            shouldThrow<IllegalArgumentException> { model.getIndexOfChild(model.root.children[0], "not a node") }
                .message should matchBundle("template_list.error.must_be_state_node", "Child")
        }

        test("parameterized") {
            forAll(
                table(
                    headers("description", "parent", "child", "expected"),
                    row(
                        "parent is null",
                        null,
                        model.root.children[1].children[0],
                        -1,
                    ),
                    row(
                        "child is null",
                        model.root.children[1],
                        null,
                        -1,
                    ),
                    row(
                        "parent is infertile",
                        model.root.children[1].children[0],
                        model.root.children[0].children[0],
                        -1,
                    ),
                    row(
                        "parent is not child's parent",
                        model.root.children[1],
                        model.root.children[0].children[1],
                        -1,
                    ),
                    row(
                        "parent is child's parent",
                        model.root.children[1],
                        model.root.children[1].children[0],
                        0,
                    ),
                    row(
                        "parent looks at UUID for matching",
                        model.root.children[0],
                        StateNode(model.list.templates[0].schemes[1].deepCopy(retainUuid = true)),
                        1,
                    ),
                )
            ) { _, parent, child, expected -> model.getIndexOfChild(parent, child) shouldBe expected }
        }
    }

    test("getParentOf") {
        test("returns an error if the node is not contained in this model") {
            shouldThrow<IllegalArgumentException> { model.getParentOf(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.parent_of_node_not_in_model")
        }

        test("parameterized") {
            forAll(
                table(
                    headers("description", "node", "expected"),
                    row("parent of root", model.root, null),
                    row("parent of template", model.root.children[0], model.root),
                    row("parent of scheme", model.root.children[1].children[0], model.root.children[1]),
                )
            ) { _, node, expected -> model.getParentOf(node) shouldBe expected }
        }
    }

    test("getPathToRoot") {
        test("returns an error if the node is not contained in this model") {
            shouldThrow<IllegalArgumentException> { model.getPathToRoot(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.path_of_node_not_in_model")
        }

        test("parameterized") {
            forAll(
                table(
                    headers("description", "node", "expected"),
                    row(
                        "path to root",
                        model.root,
                        arrayOf(model.root),
                    ),
                    row(
                        "path to template",
                        model.root.children[0],
                        arrayOf(model.root, model.root.children[0]),
                    ),
                    row(
                        "path to scheme",
                        model.root.children[1].children[0],
                        arrayOf(model.root, model.root.children[1], model.root.children[1].children[0]),
                    ),
                )
            ) { _, node, expected -> model.getParentOf(node) shouldBe expected }
        }
    }


    test("insertNode") {
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
            model.list.templates.names() shouldContainExactly listOf("Template0", "Template3", "Template1", "Template2")
        }

        test("inserts a node at the last index of the parent") {
            val node = StateNode(Template("Template3"))

            model.insertNode(model.root, node, index = 2)

            model.root.children shouldHaveSize 4
            model.root.children[2] shouldBe node
            model.list.templates.names() shouldContainExactly listOf("Template0", "Template1", "Template2", "Template3")
        }


        test("informs listeners of the insertion") {
            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val scheme = StateNode(DummyScheme())
            model.insertNode(model.root.children[0], scheme)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root.children[0]
            lastEvent!!.childIndices shouldContainExactly arrayOf(0)
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
            model.reload(TemplateList(mutableListOf()))

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val node = StateNode(Template("Template3"))
            model.insertNode(model.root, node)

            lastEvent!!.treePath.lastPathComponent shouldBe model.root
            lastEvent!!.childIndices should beEmptyIntArray()
            lastEvent!!.children should beNull()
        }
    }

    test("insertNodeAfter") {
        test("throws an error if the node to insert after is not in the parent") {
            shouldThrow<IllegalArgumentException> {
                model.insertNodeAfter(model.root, StateNode(Template()), StateNode(DummyScheme()))
            }.message should matchBundle("template_list.error.find_node_insert_parent")
        }

        test("inserts the node after the given node") {
            val node = StateNode(Template("Family"))

            model.insertNodeAfter(model.root, node, model.root.children[1])

            model.root.children[2] shouldBe node
        }
    }

    test("removeNode") {
        test("throws an error if the given node is not contained in the model") {
            shouldThrow<IllegalArgumentException> { model.removeNode(StateNode(DummyScheme())) }
                .message should matchBundle("template_list.error.remove_node_not_in_model")
        }

        test("throws an error if the given node is the root of the model") {
            shouldThrow<IllegalArgumentException> { model.removeNode(model.root) }
                .message should matchBundle("template_list.error.remove_root")
        }

        test("removes a node from the model and the underlying list") {
            val node = model.root.children[1]

            model.removeNode(node)

            model.root.children shouldHaveSize 2
            model.root.children shouldNotContain node
            model.list.templates shouldHaveSize 2
            model.list.templates shouldNotContain node.state
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


    test("fire methods") {
        var nodesChangedInvoked = 0
        var nodesInsertedInvoked = 0
        var nodesRemovedInvoked = 0
        var structureChangedInvoked = 0
        var totalInvoked = 0
        var lastEvent: TreeModelEvent? = null


        beforeEach {
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


        test("fireNodeChanged") {
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

        test("fireNodeInserted") {
            test("throws an error if a template list is given") {
                val node = StateNode(DummyScheme())

                shouldThrow<IllegalArgumentException> { model.fireNodeInserted(model.root, node, 688) }
                    .message should matchBundle("template_list.error.insert_list")
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

        test("fireNodeRemoved") {
            test("does nothing if the given node is null") {
                model.fireNodeRemoved(null, StateNode(DummyScheme()), 247)

                totalInvoked shouldBe 0
            }

            test("throws an error if a template list is given") {
                val node = StateNode(DummyScheme())

                shouldThrow<IllegalArgumentException> { model.fireNodeRemoved(model.root, node, 888) }
                    .message should matchBundle("template_list.error.remove_list")
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

        test("fireNodeStructureChanged") {
            test("does nothing if the given node is null") {
                model.fireNodeStructureChanged(null)

                totalInvoked shouldBe 0
            }

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


    test("valueForPathChanged") {
        test("throws an error") {
            shouldThrow<NotImplementedError> { model.valueForPathChanged(model.getPathToRoot(model.root), "dirt") }
                .message should matchBundle("template_list.error.change_value_by_path")
        }
    }

    test("addTreeModelListener") {
        test("invokes the added listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.reload()

            invoked shouldBe 1
        }
    }

    test("removeTreeModelListener") {
        test("no longer invokes the removed listener when an event occurs") {
            var invoked = 0
            val listener = SimpleTreeModelListener { invoked++ }

            model.addTreeModelListener(listener)
            model.reload()

            model.removeTreeModelListener(listener)
            model.reload()

            invoked shouldBe 1
        }
    }
})

/**
 * Unit tests for [StateNode].
 */
object StateNodeTest : FunSpec({
    test("canHaveChildren") {
        forAll(
            table(
                headers("state", "expected"),
                row(TemplateList(mutableListOf()), true),
                row(Template(), true),
                row(DummyScheme(), false),
            )
        ) { state, expected -> StateNode(state).canHaveChildren shouldBe expected }
    }

    test("children") {
        test("get") {
            test("throws an error for a non-template scheme") {
                shouldThrow<IllegalStateException> { StateNode(DummyScheme()).children }
                    .message should matchBundle("template_list.error.unknown_state_type", "parent")
            }

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
        }

        test("set") {
            test("throws an error for a non-template scheme") {
                shouldThrow<IllegalStateException> { run { StateNode(DummyScheme()).children = emptyList() } }
                    .message should matchBundle("template_list.error.unknown_state_type", "parent")
            }

            test("modifies the templates of a template list") {
                val list = TemplateList(mutableListOf(Template("Template0"), Template("Template1")))

                StateNode(list).children = listOf(StateNode(Template("NewTemplate")))

                list.templates.map { it.name } shouldContainExactly listOf("NewTemplate")
            }

            test("modifies the schemes of a template") {
                val template = Template(schemes = mutableListOf(DummyScheme(), DummyScheme()))

                val newScheme = DummyScheme()
                StateNode(template).children = listOf(StateNode(newScheme))

                template.schemes shouldContainExactly listOf(newScheme)
            }
        }
    }

    test("recursiveChildren") {
        test("returns the templates and schemes of a template list in depth-first order") {
            val templates = mutableListOf(
                Template("Template0", mutableListOf(DummyScheme("Scheme0"), DummyScheme("Scheme1"))),
                Template("Template1", mutableListOf(DummyScheme("Scheme1"))),
            )

            val children = StateNode(TemplateList(templates)).recursiveChildren

            val names = listOf("Template0", "Scheme0", "Scheme1", "Template1", "Scheme1")
            children.map { (it.state as Scheme).name } shouldContainExactly names
        }

        test("returns the schemes of a template") {
            val schemes = mutableListOf<Scheme>(DummyScheme("Scheme0"), DummyScheme("Scheme1"))

            val children = StateNode(Template(schemes = schemes)).recursiveChildren

            children.map { (it.state as Scheme).name } shouldContainExactly listOf("Scheme0", "Scheme1")
        }

        test("returns an empty list if the node cannot have children") {
            StateNode(DummyScheme()).recursiveChildren should beEmpty()
        }
    }


    test("contains") {
        forAll(
            table(
                //@formatter:off
                headers("description", "parent", "getChild", "expected"),
                row("list: contains itself", TemplateList(), { it }, true),
                row("list: contains other list", TemplateList(), { TemplateList() }, false),
                row("list: contains child template", TemplateList(mutableListOf(Template())), { (it as TemplateList).templates[0] }, true),
                row("list: contains non-child template", TemplateList(mutableListOf(Template("Child"))), { Template("Other") }, false),
                row("list: contains child scheme", TemplateList(mutableListOf(Template(schemes = mutableListOf(DummyScheme())))), { (it as TemplateList).templates[0].schemes[0]}, true),
                row("list: contains non-child scheme", TemplateList(mutableListOf(Template(schemes = mutableListOf(DummyScheme())))), { DummyScheme() }, false),
                row("template: contains template list", Template(), { TemplateList() }, false),
                row("template: contains itself", Template(), { it }, true),
                row("template: contains other template", Template(), { Template() }, false),
                row("template: contains child scheme", Template(schemes = mutableListOf(DummyScheme())), { (it as Template).schemes[0] }, true),
                row("template: contains non-child scheme", Template(schemes = mutableListOf(DummyScheme())), { DummyScheme() }, false),
                row("scheme: contains template list", DummyScheme(), { TemplateList() }, false),
                row("scheme: contains template", DummyScheme(), { Template() }, false),
                row("scheme: contains itself", DummyScheme(), { it }, true),
                row("scheme: contains other scheme", DummyScheme(), { DummyScheme() }, false),
                //@formatter:on
            )
        ) { _, parent: State, getChild: (State) -> State, expected ->
            StateNode(parent).contains(StateNode(getChild(parent))) shouldBe expected
        }
    }


    test("equals") {
        test("does not equal an object of another class") {
            StateNode(DummyScheme()) shouldNotBe 299
        }

        test("equals itself") {
            val node = StateNode(DummyScheme())

            node shouldBe node
        }

        test("equals an identical object") {
            val scheme = DummyScheme()

            StateNode(scheme) shouldBe StateNode(scheme)
        }

        test("does not equal a different object") {
            val node = StateNode(DummyScheme())

            node shouldNotBe StateNode(DummyScheme())
        }
    }

    test("hashCode") {
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

        test("does not equal the hashcode of a different object") {
            val node1 = StateNode(DummyScheme())
            val node2 = StateNode(DummyScheme())

            node1.hashCode() shouldNotBe node2.hashCode()
        }
    }
})
