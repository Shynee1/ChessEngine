# ChessEngine

A fast and powerful chess engine built entirely in Java, capable of playing at a strong club level. The engine was designed from the ground up with a focus on efficient search, accurate evaluation, and clean architecture.

📄 **Paper**: [Technical Writeup](https://docs.google.com/document/d/1FmzyMskp5cH1NqH1BmDZzTjVIRv_IGA1aB3PortDNeE/edit?tab=t.0)

---

## 🚀 Features

- 🧩 Fully custom chess engine — no external chess libraries used
- 🧠 Plays at ~2000+ ELO strength on Chess.com
- ⚙️ Real-time evaluation output and adjustable search depth
- ⏱️ Highly optimized search using:
  - NegaMax algorithm with alpha-beta pruning
  - Principal Variation Search (PVS)
  - Move ordering heuristics (e.g., killer moves, history heuristic)
  - Transposition tables with Zobrist hashing for efficient position lookup
  - Static evaluation function with positional heuristics
  - Basic opening book and endgame awareness

---

## 🛠️ Technical Details

- **Language**: Java  
- **Rendering & Input**: Built with [LibGDX](https://libgdx.com/) for cross-platform graphical support
- **Board Representation**: Bitboard-style system optimized for fast move generation
- **Move Generation**: Custom legal move generator with pseudo-legal pruning
- **Search Depth**: Adjustable; supports deep searches with pruning and optimization
- **Engine Loop**: Handles iterative deepening and timed move selection

The core of the engine is structured for readability and performance, making it both a learning tool and a competitive engine.

---

## 📚 Learning Resources

Some resources that were helpful during development:

- [Sebastian Lague’s Chess Engine Series](https://www.youtube.com/watch?v=U4ogK0MIzqk)
- [ChessProgramming Wiki](https://www.chessprogramming.org/Main_Page)

---

## 📦 Getting Started

_Clone and run the project using any standard Java IDE with Gradle support. Make sure LibGDX is correctly configured._

```bash
git clone https://github.com/yourusername/ChessEngine
cd ChessEngine
# Open with IntelliJ or Eclipse and run the main class
