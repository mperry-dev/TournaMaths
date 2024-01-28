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

  // Method to render the KaTeX
  function renderKaTeX(text, elementToRender) {
    try {
      var renderedEquation = katex.renderToString(text, {
        throwOnError: false,
        displayMode: true,
      });
      // Create a temporary element to hold the rendered equation
      var tempDiv = $("<div>").html(renderedEquation);

      // Apply the nonce to any inline styles in the temporary element
      tempDiv.find("[style]").each(function () {
        $(this).attr("nonce", nonce);
      });

      // Replace the inner HTML of the element with the processed content
      $(elementToRender).html(tempDiv.html());
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
