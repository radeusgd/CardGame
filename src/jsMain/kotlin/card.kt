import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position
import com.radeusgd.trachonline.messages.FlipCard
import com.radeusgd.trachonline.messages.MakeStack
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.PickStack
import com.radeusgd.trachonline.messages.PutOnStack
import com.radeusgd.trachonline.messages.ShuffleStack
import kotlin.math.max
import kotlin.math.min
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.dom.hasClass
import kotlinx.html.IMG
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onDoubleClickFunction
import kotlinx.html.js.onDragEndFunction
import kotlinx.html.js.onDragEnterFunction
import kotlinx.html.js.onDragLeaveFunction
import kotlinx.html.js.onDragStartFunction
import org.w3c.dom.DragEvent
import org.w3c.dom.Element
import org.w3c.dom.Image
import org.w3c.dom.get
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.br
import react.dom.span
import styled.css
import styled.styledA
import styled.styledDiv
import styled.styledImg

// TODO parent board id may be obsolete if we are able to detect boards by position, but we still need some notion of what boards are available
data class EntityProps(var placedEntity: PlacedEntity, var baseSize: Float, var parentBoardId: Uuid) : RProps

interface EntityState : RState

@JsExport
class EntityView(props: EntityProps) : RComponent<EntityProps, EntityState>(props) {

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.placedEntityCss(props.placedEntity)
            }
            val entity = props.placedEntity.entity
            val setupDragging: IMG.(imagePath: String) -> Unit = { imagePath ->
                onDragStartFunction = { event ->
                    val dragged = (event.asDynamic().nativeEvent as? DragEvent)
                    val img = Image()
                    img.src = imagePath
                    // TODO I cannot get to make this image smaller, for now let it serve as 'preview' feature
//                    img.style.width = "40px"
//                    img.style.height = "40px"
                    dragged?.dataTransfer?.setDragImage(img, 0, 0)
                }
                onDragEndFunction = { event ->
                    console.log(event)
                    val dragged = (event.asDynamic().nativeEvent as? DragEvent)
                    dragged?.let {
                        var hasBeenProcessed = false
                        val elementsBelow = document.elementsFromPoint(it.pageX, it.pageY)
                        fun findByClass(className: String): Element? {
                            return elementsBelow.find { it.hasClass(className) }
                        }

                        val possibleStack = findByClass(StackDragTargetClassName)
                        val possibleStackMaker = findByClass(Gameboard.StackMakerClass)
                        val possibleBoard = findByClass(Gameboard.BoardAreaClass)

                        if (entity is Card && possibleStack != null) {
                            possibleStack.attributes["data-stackid"]?.value?.let {
                                console.log("Stack $it")
                                val uuid: Uuid = uuidFrom(it)
                                sendMessage(PutOnStack(stackUuid = uuid, cardUuid = entity.uuid))
                                hasBeenProcessed = true
                            }
                        } else if (entity is Card && possibleStackMaker != null) {
                            sendMessage(MakeStack(cardUuid = entity.uuid))
                        } else if (possibleBoard != null) {
                            possibleBoard.attributes["data-boardid"]?.value?.let { boardId ->
                                val rect = possibleBoard.getBoundingClientRect()
                                console.log("Board $boardId")

                                val vw = max(document.documentElement?.clientWidth ?: 0, window.innerWidth)
                                val vh = max(document.documentElement?.clientHeight ?: 0, window.innerHeight)
                                val vmin = if (vw == 0) vh else if (vh == 0) vw else min(vw, vh)
                                if (vmin == 0) {
                                    console.error("Could not estimate viewport size!")
                                } else {
                                    val x: Float = ((it.clientX - rect.left) / vmin * 100f).toFloat()
                                    val y: Float = ((it.clientY - rect.top) / vmin * 100f).toFloat()
                                    console.log(x to y)
                                    val depth = 0 // TODO depth
                                    val destination = BoardDestination(uuidFrom(boardId), Position(x, y, depth))
                                    sendMessage(MoveEntity(entity.uuid, destination))
                                }
                            }
                        } else {
                            console.log("No board nor stack target found")
                        }
                    }
                }
            }

            when (entity) {
                is CardStack ->
                    styledDiv {
                        css {
                            +GameStyles.cardStackBorderStyle
                        }

                        span {
                            +"(${entity.cards.size} cards)"
                            attrs {
                                classes += StackDragTargetClassName
                                attributes.set("data-stackid", entity.uuid.toString())
                                onDragEnterFunction = { _ ->
                                    // TODO set state to higlight a stack when another card is being dragged onto it
                                }
                                onDragLeaveFunction = { _ ->
                                    // TODO set state to stop highlighting the stack when another card is being dragged onto it
                                }
                            }
                        }
                        styledA {
                            css {
                                +GameStyles.hiddenAStyle
                            }
                            +"\uD83D\uDD04"
                            attrs {
                                onClickFunction = { _ ->
                                    sendMessage(ShuffleStack(entity.uuid))
                                }
                            }
                        }
                        br { }
                        val imagePath = "/static/" + (entity.cards.firstOrNull()?.getCurrentImage() ?: "error.jpg")
                        styledImg(src = imagePath) {
                            css {
                                +GameStyles.cardStyle(props.baseSize)
                            }
                            attrs {
                                setupDragging(imagePath)
                                onDoubleClickFunction = { _ ->
                                    sendMessage(PickStack(entity.uuid))
                                }
                            }
                        }
                    }
                is Card -> {
                    val imagePath = "/static/" + entity.getCurrentImage()
                    styledImg(src = imagePath) {
                        css {
                            +GameStyles.cardStyle(props.baseSize)
                        }
                        attrs {
                            setupDragging(imagePath)
                            onDoubleClickFunction = { _ ->
                                sendMessage(FlipCard(entity.uuid))
                            }
                        }
                    }
                }
            }
        }
    }

    private val StackDragTargetClassName = "stack-drag-target"
}
