/* =====================================================
   SUDOKUARENA - GAME SCRIPT
   Everything the page displays (names, stats, leaderboard, profile,
   results) is rendered by JSP. This file only handles the live board:
   selecting cells, notes, undo, timer, pause and the calls to the
   GameServlet (start / hint / finish / abandon).
===================================================== */

"use strict";

(() => {
    const $ = (selector, root = document) => root.querySelector(selector);
    const $$ = (selector, root = document) =>
        Array.from(root.querySelectorAll(selector));

    const shell = $("#appShell");
    const CTX = shell.dataset.ctx || "";

    const state = {
        page: shell.dataset.startPage || "home",
        active: false,          // a puzzle is being played
        busy: false,            // a request is in flight
        difficulty: "Easy",
        board: [],
        original: [],
        selected: null,         // { row, col }
        history: [],
        notes: {},
        hintsLeft: 3,
        wrong: 0,
        seconds: 0,
        timer: null,
        paused: false,
        notesMode: false
    };

    /* =====================================================
       1. TOAST
    ===================================================== */

    function showToast(message, type = "success") {
        const toast = $("#toast");
        toast.textContent = message;
        toast.className = `toast show ${type}`;

        clearTimeout(showToast.timeout);
        showToast.timeout = setTimeout(() => {
            toast.className = "toast";
        }, 3000);
    }

    /* =====================================================
       2. SERVER CALLS
    ===================================================== */

    async function post(path, data = {}) {
        const response = await fetch(`${CTX}/${path}`, {
            method: "POST",
            credentials: "same-origin",
            headers: { "Content-Type": "application/x-www-form-urlencoded; charset=UTF-8" },
            body: new URLSearchParams(data)
        });

        let json = {};
        try {
            json = await response.json();
        } catch (error) {
            /* the server did not send JSON */
        }

        if (response.status === 401) {
            window.location.href = `${CTX}/login.jsp`;
            throw new Error("Please log in again.");
        }

        if (!response.ok) {
            throw new Error(json.error || "Something went wrong. Please try again.");
        }

        return json;
    }

    /* =====================================================
       3. PAGE NAVIGATION
    ===================================================== */

    function showPage(name) {
        const target = $(`#page-${name}`);
        if (!target) {
            if (name !== "home") showPage("home");
            return;
        }

        $$(".page").forEach(page => page.classList.remove("active"));
        target.classList.add("active");
        state.page = name;

        const highlight = { game: "levels", results: "home" }[name] || name;
        $$(".sidebar-nav [data-page]").forEach(button => {
            button.classList.toggle("active", button.dataset.page === highlight);
        });

        window.scrollTo({ top: 0, behavior: "smooth" });
    }

    /* Leaving a running puzzle records it as a loss, so ask first. */
    async function navigate(page) {
        if (state.page === "game" && state.active && page !== "game") {
            if (!confirm("Leave this puzzle? It will be recorded as a loss.")) return;

            stopTimer();
            state.active = false;

            try {
                await post("game/abandon");
            } catch (error) {
                /* the next page load will still show the saved stats */
            }

            /* Reload so the JSP shows the updated statistics. */
            window.location.href = `${CTX}/home?page=${page}`;
            return;
        }

        showPage(page);
    }

    /* =====================================================
       4. SUDOKU RULES (used for live conflict highlighting)
    ===================================================== */

    function isValidPlacement(board, row, col, number) {
        for (let i = 0; i < 9; i++) {
            if (i !== col && board[row][i] === number) return false;
            if (i !== row && board[i][col] === number) return false;
        }

        const boxRow = Math.floor(row / 3) * 3;
        const boxCol = Math.floor(col / 3) * 3;

        for (let r = boxRow; r < boxRow + 3; r++) {
            for (let c = boxCol; c < boxCol + 3; c++) {
                if ((r !== row || c !== col) && board[r][c] === number) return false;
            }
        }

        return true;
    }

    function findConflicts() {
        const conflicts = new Set();

        for (let row = 0; row < 9; row++) {
            for (let col = 0; col < 9; col++) {
                const value = state.board[row][col];
                if (value !== 0 && !isValidPlacement(state.board, row, col, value)) {
                    conflicts.add(row * 9 + col);
                }
            }
        }

        return conflicts;
    }

    function isBoardFull() {
        return state.board.every(row => row.every(value => value !== 0));
    }

    /* =====================================================
       5. START A GAME
    ===================================================== */

    async function startGame(level) {
        if (state.busy) return;
        state.busy = true;

        try {
            const game = await post("game/start", { difficulty: level });

            state.difficulty = game.difficulty;
            state.board = game.puzzle.map(row => [...row]);
            state.original = game.puzzle.map(row => [...row]);
            state.selected = null;
            state.history = [];
            state.notes = {};
            state.hintsLeft = game.hintsLeft;
            state.wrong = 0;
            state.seconds = 0;
            state.paused = false;
            state.notesMode = false;
            state.active = true;

            stopTimer();

            $("#gameLevel").textContent = game.difficulty;
            $("#timer").textContent = "00:00";
            $("#wrongMovesCount").textContent = "0";
            $("#hintsLeft").textContent = state.hintsLeft;
            $("#gameMessage").textContent = "Select an empty cell, then choose a number.";
            updateNotesUI();
            updatePauseUI();

            buildBoard();
            buildNumberPad();
            paintBoard();
            updateProgress();

            showPage("game");
            startTimer();
        } catch (error) {
            showToast(error.message, "error");
        } finally {
            state.busy = false;
        }
    }

    /* =====================================================
       6. BOARD
    ===================================================== */

    function buildBoard() {
        const boardElement = $("#sudokuBoard");
        boardElement.innerHTML = "";

        for (let index = 0; index < 81; index++) {
            const cell = document.createElement("button");
            cell.type = "button";
            cell.className = "sudoku-cell";
            cell.dataset.row = Math.floor(index / 9);
            cell.dataset.col = index % 9;
            cell.setAttribute(
                "aria-label",
                `Row ${Math.floor(index / 9) + 1}, column ${(index % 9) + 1}`
            );
            boardElement.appendChild(cell);
        }
    }

    function paintBoard() {
        const conflicts = findConflicts();
        const selected = state.selected;
        const selectedValue = selected ? state.board[selected.row][selected.col] : 0;

        $("#sudokuBoard").classList.toggle("paused", state.paused);

        $$(".sudoku-cell").forEach((cell, index) => {
            const row = Math.floor(index / 9);
            const col = index % 9;
            const value = state.board[row][col];

            cell.className = "sudoku-cell";
            cell.disabled = state.paused;

            if (state.original[row][col] !== 0) cell.classList.add("given");

            if (selected) {
                if (row === selected.row && col === selected.col) {
                    cell.classList.add("selected");
                } else {
                    const sameBox =
                        Math.floor(row / 3) === Math.floor(selected.row / 3) &&
                        Math.floor(col / 3) === Math.floor(selected.col / 3);

                    if (row === selected.row || col === selected.col || sameBox) {
                        cell.classList.add("related");
                    }

                    if (selectedValue !== 0 && value === selectedValue) {
                        cell.classList.add("same-number");
                    }
                }
            }

            if (conflicts.has(index)) cell.classList.add("error");

            /* Content: the number, or small pencil notes */
            cell.textContent = "";
            const cellNotes = state.notes[`${row}-${col}`];

            if (value !== 0) {
                cell.textContent = value;
            } else if (cellNotes && cellNotes.length) {
                const wrapper = document.createElement("div");
                wrapper.className = "cell-notes";

                for (let n = 1; n <= 9; n++) {
                    const span = document.createElement("span");
                    span.textContent = cellNotes.includes(n) ? n : "";
                    wrapper.appendChild(span);
                }

                cell.appendChild(wrapper);
            }
        });
    }

    function buildNumberPad() {
        const pad = $("#numberPad");
        pad.innerHTML = "";

        for (let number = 1; number <= 9; number++) {
            const button = document.createElement("button");
            button.type = "button";
            button.className = "number-button";
            button.textContent = number;
            button.dataset.number = number;
            pad.appendChild(button);
        }
    }

    function selectCell(row, col) {
        if (!state.active || state.paused) return;
        state.selected = { row, col };
        paintBoard();
    }

    function moveSelection(rowChange, colChange) {
        if (!state.selected) {
            selectCell(0, 0);
            return;
        }

        const row = Math.max(0, Math.min(8, state.selected.row + rowChange));
        const col = Math.max(0, Math.min(8, state.selected.col + colChange));
        selectCell(row, col);
    }

    /* =====================================================
       7. MOVES
    ===================================================== */

    function canPlay() {
        return state.active && !state.paused;
    }

    function saveHistory() {
        state.history.push({
            board: state.board.map(row => [...row]),
            notes: JSON.parse(JSON.stringify(state.notes))
        });

        if (state.history.length > 100) state.history.shift();
    }

    function enterNumber(number) {
        if (!canPlay()) return;

        if (!state.selected) {
            showToast("Select an empty cell first.", "error");
            return;
        }

        const { row, col } = state.selected;

        if (state.original[row][col] !== 0) {
            showToast("You cannot change an original clue.", "error");
            return;
        }

        const key = `${row}-${col}`;

        if (state.notesMode) {
            if (state.board[row][col] !== 0) {
                showToast("Erase the cell before adding notes.", "error");
                return;
            }

            saveHistory();

            const current = state.notes[key] || [];
            state.notes[key] = current.includes(number)
                ? current.filter(item => item !== number)
                : [...current, number].sort();

            paintBoard();
            return;
        }

        saveHistory();
        state.board[row][col] = number;
        delete state.notes[key];

        if (!isValidPlacement(state.board, row, col, number)) {
            state.wrong++;
            $("#wrongMovesCount").textContent = state.wrong;
            $("#gameMessage").textContent = "That number clashes with its row, column or box.";
        } else {
            $("#gameMessage").textContent = "Good move. Keep going!";
        }

        paintBoard();
        updateProgress();
        checkCompletion();
    }

    function undoMove() {
        if (!canPlay()) return;

        if (!state.history.length) {
            showToast("Nothing to undo.", "error");
            return;
        }

        const previous = state.history.pop();
        state.board = previous.board;
        state.notes = previous.notes;

        paintBoard();
        updateProgress();
        showToast("Last move undone.");
    }

    function eraseCell() {
        if (!canPlay() || !state.selected) return;

        const { row, col } = state.selected;

        if (state.original[row][col] !== 0) {
            showToast("Original clues cannot be erased.", "error");
            return;
        }

        saveHistory();
        state.board[row][col] = 0;
        delete state.notes[`${row}-${col}`];

        paintBoard();
        updateProgress();
    }

    async function giveHint() {
        if (!canPlay() || state.busy) return;

        if (!state.selected) {
            showToast("Select an empty cell for a hint.", "error");
            return;
        }

        const { row, col } = state.selected;

        if (state.original[row][col] !== 0) {
            showToast("Choose a cell that was originally empty.", "error");
            return;
        }

        if (state.board[row][col] !== 0) {
            showToast("That cell already contains a number.", "error");
            return;
        }

        state.busy = true;

        try {
            const hint = await post("game/hint", { row, col });

            saveHistory();
            state.board[row][col] = hint.value;
            delete state.notes[`${row}-${col}`];
            state.hintsLeft = hint.hintsLeft;
            $("#hintsLeft").textContent = state.hintsLeft;

            paintBoard();
            updateProgress();
            showToast("Hint added.");
            checkCompletion();
        } catch (error) {
            showToast(error.message, "error");
        } finally {
            state.busy = false;
        }
    }

    function checkBoard() {
        if (!canPlay()) return;

        const conflicts = findConflicts().size;

        if (conflicts > 0) {
            showToast(`${conflicts} conflicting cell(s) found.`, "error");
        } else if (!isBoardFull()) {
            showToast("No rule conflicts found so far.");
        } else {
            checkCompletion();
        }
    }

    /* =====================================================
       8. NOTES, PAUSE, RESET
    ===================================================== */

    function updateNotesUI() {
        $("#notesButton").textContent = state.notesMode ? "✎ Notes on" : "✎ Notes off";
        $("#notesButton").classList.toggle("active", state.notesMode);
        $("#notesStatus").textContent = state.notesMode ? "On" : "Off";
    }

    function toggleNotes() {
        if (!canPlay()) return;

        state.notesMode = !state.notesMode;
        updateNotesUI();
        showToast(state.notesMode ? "Notes mode enabled." : "Notes mode disabled.");
    }

    function updatePauseUI() {
        $("#pauseButton").textContent = state.paused ? "▶ Resume" : "Ⅱ Pause";
        $$(".number-button").forEach(button => {
            button.disabled = state.paused;
        });
    }

    function pauseGame() {
        if (!state.active) return;

        state.paused = !state.paused;

        if (state.paused) {
            stopTimer();
            showToast("Game paused.");
        } else {
            startTimer();
            showToast("Game resumed.");
        }

        updatePauseUI();
        paintBoard();
    }

    function resetGame() {
        if (!state.active) return;
        if (!confirm("Reset this puzzle to its original state?")) return;

        state.board = state.original.map(row => [...row]);
        state.history = [];
        state.notes = {};
        state.selected = null;
        state.seconds = 0;
        state.wrong = 0;
        state.paused = false;
        state.notesMode = false;

        $("#timer").textContent = "00:00";
        $("#wrongMovesCount").textContent = "0";

        updateNotesUI();
        updatePauseUI();
        paintBoard();
        updateProgress();
        stopTimer();
        startTimer();

        showToast("Puzzle reset.");
    }

    /* =====================================================
       9. PROGRESS AND COMPLETION
    ===================================================== */

    function updateProgress() {
        const filled = state.board.flat().filter(value => value !== 0).length;
        const percent = Math.round((filled / 81) * 100);

        $("#filledCount").textContent = `${filled} / 81 cells`;
        $("#progressPercent").textContent = `${percent}%`;
        $("#progressBar").style.width = `${percent}%`;
    }

    function checkCompletion() {
        if (!isBoardFull()) return;

        if (findConflicts().size > 0) {
            showToast("Some answers are incorrect. Check your board.", "error");
            return;
        }

        finishGame();
    }

    /* The server verifies the board and stores the result in Oracle. */
    async function finishGame() {
        if (state.busy) return;
        state.busy = true;
        stopTimer();

        try {
            const result = await post("game/finish", {
                board: state.board.flat().join(""),
                seconds: state.seconds,
                wrong: state.wrong
            });

            state.active = false;
            window.location.href = `${CTX}/${result.redirect}`;
        } catch (error) {
            showToast(error.message, "error");
            state.busy = false;
            startTimer();
        }
    }

    /* =====================================================
       10. TIMER
    ===================================================== */

    function formatTime(total) {
        const minutes = String(Math.floor(total / 60)).padStart(2, "0");
        const seconds = String(total % 60).padStart(2, "0");
        return `${minutes}:${seconds}`;
    }

    function startTimer() {
        stopTimer();
        if (!state.active || state.paused) return;

        state.timer = setInterval(() => {
            state.seconds++;
            $("#timer").textContent = formatTime(state.seconds);
        }, 1000);
    }

    function stopTimer() {
        if (state.timer !== null) {
            clearInterval(state.timer);
            state.timer = null;
        }
    }

    /* =====================================================
       11. INVITE CODE
    ===================================================== */

    async function copyInviteCode() {
        const input = $("#inviteCode");

        try {
            await navigator.clipboard.writeText(input.value);
            showToast("Invite code copied.");
        } catch (error) {
            input.select();
            showToast("Press Ctrl+C to copy the selected code.", "error");
        }
    }

    /* =====================================================
       12. EVENTS
    ===================================================== */

    function bindEvents() {
        /* Page navigation buttons (sidebar, banners, back buttons) */
        $$("[data-page]").forEach(button => {
            button.addEventListener("click", () => navigate(button.dataset.page));
        });

        /* Level cards on Home and Play, and "Play again" on the results page */
        $$("[data-level]").forEach(button => {
            button.addEventListener("click", () => startGame(button.dataset.level));
        });

        /* Board and number pad use one listener each */
        $("#sudokuBoard").addEventListener("click", event => {
            const cell = event.target.closest(".sudoku-cell");
            if (cell) selectCell(Number(cell.dataset.row), Number(cell.dataset.col));
        });

        $("#numberPad").addEventListener("click", event => {
            const button = event.target.closest("[data-number]");
            if (button) enterNumber(Number(button.dataset.number));
        });

        /* Game tools */
        $("#undoButton").addEventListener("click", undoMove);
        $("#hintButton").addEventListener("click", giveHint);
        $("#eraseButton").addEventListener("click", eraseCell);
        $("#notesButton").addEventListener("click", toggleNotes);
        $("#checkButton").addEventListener("click", checkBoard);
        $("#pauseButton").addEventListener("click", pauseGame);
        $("#resetButton").addEventListener("click", resetGame);

        $("#copyInviteButton").addEventListener("click", copyInviteCode);

        /* Keyboard: 1-9, Backspace/Delete, arrow keys */
        document.addEventListener("keydown", event => {
            if (state.page !== "game" || !canPlay()) return;

            if (/^[1-9]$/.test(event.key)) {
                enterNumber(Number(event.key));
            } else if (event.key === "Backspace" || event.key === "Delete") {
                eraseCell();
            } else if (event.key === "ArrowUp") {
                event.preventDefault();
                moveSelection(-1, 0);
            } else if (event.key === "ArrowDown") {
                event.preventDefault();
                moveSelection(1, 0);
            } else if (event.key === "ArrowLeft") {
                event.preventDefault();
                moveSelection(0, -1);
            } else if (event.key === "ArrowRight") {
                event.preventDefault();
                moveSelection(0, 1);
            }
        });
    }

    /* =====================================================
       13. START
    ===================================================== */

    bindEvents();
    showPage(state.page);

    /* Messages from servlets (profile saved, friend added, errors...) */
    if (shell.dataset.flash) {
        showToast(shell.dataset.flash, shell.dataset.flashType === "error" ? "error" : "success");
    }
})();
