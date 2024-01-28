$(document).ready(function () {
  // Input box for constructing question
  let equationInput = $("#equation-input");

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

  // Method to render the KaTeX.
  // We use katex.render to render directly into a DOM element, avoiding CSP violations and increasing security.
  function renderKaTeX(text, elementToRender) {
    try {
      // Create a temporary container for rendering
      var tempContainer = document.createElement("div");

      // Render KaTeX directly into the temporary container.
      // throwOnError=true since handling by KaTeX when false gives CSP violation.
      katex.render(text, tempContainer, {
        throwOnError: true,
        displayMode: true,
      });

      // Move the processed content to the target element
      $(elementToRender).empty().append($(tempContainer).contents());
    } catch (e) {
      // Create a new element for the error message
      var errorMessage = $("<span>").text("Error in rendering: " + e.message);

      // Apply red color style to the error message
      errorMessage.css("color", "red");

      // Append the styled error message to the element
      $(elementToRender).empty().append(errorMessage);
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
