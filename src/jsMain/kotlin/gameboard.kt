import com.radeusgd.trachonline.board.GameSnapshot
import react.RBuilder
import react.RComponent
import react.RProps
import react.RState
import react.dom.p
import styled.css
import styled.styledDiv

data class GameboardProps(var gameSnapshot: GameSnapshot) : RProps

interface GameboardState : RState

@JsExport
class Gameboard(props: GameboardProps) : RComponent<GameboardProps, GameboardState>(props) {

    val playerPersonalAreaScale = 3f

    override fun RBuilder.render() {
        styledDiv {
            css {
                +GameStyles.mainAreaContainer
            }
            +"Основная Область"

            child(BoardAreaView::class) {
                attrs {
                    board = props.gameSnapshot.mainArea
                    entityBaseSize = 5f
                }
            }
        }
        styledDiv {
            css {
                +GameStyles.playersContainer
            }
            +"Игроки"

            props.gameSnapshot.players.forEach {
                p {
                    +"${it.name}: ${it.privateAreaCount} kart na ręce"
                }

                styledDiv {
                    css {
                        +GameStyles.personalArea(playerPersonalAreaScale)
                    }

                    child(BoardAreaView::class) {
                        attrs {
                            board = it.personalArea
                            entityBaseSize = playerPersonalAreaScale
                        }
                    }
                }
            }
        }
        styledDiv {
            css {
                +GameStyles.privateAreaContainer
            }
            +"Частная Территория"

            child(BoardAreaView::class) {
                attrs {
                    board = props.gameSnapshot.privateArea
                    entityBaseSize = 5f
                }
            }
        }
    }
}
