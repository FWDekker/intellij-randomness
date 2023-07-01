package com.fwdekker.randomness.template

import com.fwdekker.randomness.DummyScheme
import com.fwdekker.randomness.Scheme
import com.fwdekker.randomness.ui.SimpleTreeModelListener
import io.kotest.core.spec.style.DescribeSpec
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener


/**
 * Unit tests for [TemplateJTreeModel].
 */
object TemplateJTreeModelTest : DescribeSpec({
    lateinit var model: TemplateJTreeModel


    beforeEach {
        model = TemplateJTreeModel(
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

                assertThat(model.list.templates[0].schemes.map { it.name })
                    .containsExactly("bell", "people", "hot")
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

            val scheme = DummyScheme.from("fortune")
            model.insertNode(model.root.children[0], StateNode(scheme))

            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root.children[0])
            assertThat(lastEvent!!.childIndices).containsExactly(0)
            assertThat(lastEvent!!.children).containsExactly(StateNode(scheme))
        }

        it("informs listeners of a node insertion if the root's not-only child was inserted") {
            model.reload(TemplateList(listOf(Template(name = "Every"))))

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val template = Template(name = "Deed")
            model.insertNode(model.root, StateNode(template))

            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            assertThat(lastEvent!!.childIndices).containsExactly(1)
            assertThat(lastEvent!!.children).containsExactly(StateNode(template))
        }

        it("informs listeners of a structure change if the root's now-only child was inserted") {
            model.reload(TemplateList(emptyList()))

            var lastEvent: TreeModelEvent? = null
            model.addTreeModelListener(SimpleTreeModelListener { lastEvent = it })

            val template = Template(name = "Dot")
            model.insertNode(model.root, StateNode(template))

            assertThat(lastEvent!!.treePath.lastPathComponent).isEqualTo(model.root)
            assertThat(lastEvent!!.childIndices).isEmpty()
            assertThat(lastEvent!!.children).isNull()
        }
    }

    describe("insertNodeAfter") {
        it("throws an error if the node to insert after is not in the parent") {
            assertThatThrownBy {
                model.insertNodeAfter(
                    model.root,
                    StateNode(Template()),
                    StateNode(DummyScheme())
                )
            }
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
object StateNodeTest : DescribeSpec({
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
