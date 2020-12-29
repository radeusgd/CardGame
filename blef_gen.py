import json

deck = {
    "name": "Blef",
    "basePath": "bridge-cards",
    "backName": "gray_back.png",
    "cards": [
        {
            "name": f"{rank}{suit}.png",
            "count": 1
        }
        for suit in ["C", "D", "H", "S"]
        for rank in ["9", "10", "A", "J", "K", "Q"]
    ]
}
game = {"decks": [deck]}
print(json.dumps(game))
