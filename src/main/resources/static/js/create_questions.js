$(document).ready(function () {
  // Input box for constructing question
  let equationInput = $("#equation-input");
  const nonce = $("#nonceHolder").data("nonce");

  // Render the equation as the user clicks buttons
  $(".symbol-button").click(function () {
    let symbol = $(this).data("symbol");
    insertAtCursor(equationInput[0], symbol);
    renderKaTeX(equationInput.val(), "#question-preview");
  });

  // Render the equation as the user types
  equationInput.on("input", function () {
    renderKaTeX($(this).val(), "#question-preview");
  });

  // Method to render the KaTeX. We inject nonce to inline KaTeX styles so this doesn't violate CSP.
  function renderKaTeX(text, elementToRender) {
    try {
      // Create a temporary container for rendering
      var tempContainer = document.createElement("div");

      // Render KaTeX directly into the temporary container
      katex.render(text, tempContainer, {
        throwOnError: false,
        displayMode: true,
      });

      // Apply the nonce to any inline styles
      $(tempContainer)
        .find("[style]")
        .each(function () {
          $(this).attr("nonce", nonce);
        });

      // Move the processed content to the target element
      $(elementToRender).empty().append($(tempContainer).contents());
    } catch (e) {
      $(elementToRender).text("Error in rendering: " + e.message);
    }
  }

  // Render KaTeX for each question's equation in the list of equations
  $(".katex-equation").each(function () {
    let equation = $(this).text();
    renderKaTeX(equation, this);
  });

  // Method so that symbols are inserted at the user's cursor position
  function insertAtCursor(input, textToInsert) {
    let startPos = input.selectionStart;
    let endPos = input.selectionEnd;
    let cursorPos = startPos;
    let textBefore = input.value.substring(0, startPos);
    let textAfter = input.value.substring(endPos, input.value.length);
    input.value = textBefore + textToInsert + textAfter;

    // Adjust cursor position to be in middle of curly or round backets if they exist
    if (textToInsert.includes("{}")) {
      cursorPos += textToInsert.indexOf("{") + 1; // Move cursor inside the curly brackets
    } else if (textToInsert.includes("()")) {
      cursorPos += textToInsert.indexOf("(") + 1; // Move cursor inside the round brackets
    } else {
      cursorPos += textToInsert.length; // Default cursor position after the inserted text
    }

    input.selectionStart = cursorPos;
    input.selectionEnd = cursorPos;
    input.focus();
  }
});
