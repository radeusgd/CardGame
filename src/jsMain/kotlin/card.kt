import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuidFrom
import com.radeusgd.trachonline.board.BoardDestination
import com.radeusgd.trachonline.board.Card
import com.radeusgd.trachonline.board.CardStack
import com.radeusgd.trachonline.board.PlacedEntity
import com.radeusgd.trachonline.board.Position
import com.radeusgd.trachonline.messages.FlipCard
import com.radeusgd.trachonline.messages.MoveEntity
import com.radeusgd.trachonline.messages.PickStack
import com.radeusgd.trachonline.messages.PutOnStack
import com.radeusgd.trachonline.messages.ShuffleStack
import kotlin.math.max
import kotlinx.browser.document
import kotlinx.browser.window
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.dom.hasClass
import kotlinx.html.IMG
import kotlinx.html.classes
import kotlinx.html.js.onClickFunction
import kotlinx.html.js.onDoubleClickFunction
import kotlinx.html.js.onDragEndFunction
import kotlinx.html.js.onDragEnterFunction
import kotlinx.html.js.onDragFunction
import kotlinx.html.js.onDragLeaveFunction
import kotlinx.html.js.onDragStartFunction
import kotlinx.html.js.onDropFunction
import kotlinx.html.style
import org.w3c.dom.DragEvent
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
                        var wasPutOnStack = false
                        val targetElement = document.elementFromPoint(it.pageX, it.pageY)
                        targetElement?.let {
                            console.log("Target element", it)
                            if (it.hasClass(StackDragTargetClassName) && entity is Card) {
                                it.attributes.get("data-stackid")?.value?.let {
                                    console.log(it)
                                    val uuid: Uuid = uuidFrom(it)
                                    sendMessage(PutOnStack(stackUuid = uuid, cardUuid = entity.uuid))
                                    wasPutOnStack = true
                                }
                            }
                        }

                        if (wasPutOnStack) return@let

                        val vw = max(document.documentElement?.clientWidth ?: 0, window.innerWidth)
                        if (vw == 0) {
                            console.error("Could not estimate viewport width!")
                        } else {
                            val x: Float = (it.pageX / vw * 100f).toFloat()
                            val y: Float = (it.pageY / vw * 100f).toFloat()
                            console.log(x to y)
                            // TODO possibility to move to different board
                            // TODO how to detect board id based on position???
                            // TODO we may be able to detect boards by the target data
                            val board = props.parentBoardId
                            val depth = 0 // TODO depth
                            val destination = BoardDestination(board, Position(x, y, depth))
                            sendMessage(MoveEntity(entity.uuid, destination))
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
