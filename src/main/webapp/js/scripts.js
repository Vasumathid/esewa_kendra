
$(document).ready(function () {
    function handleCaseTypeChange(value) {
        const newCaseOptions = document.getElementById('newCaseOptions');
        const oldCaseOptions = document.getElementById('oldCaseOptions');
        
        if (value === 'newcase') {
            newCaseOptions.style.display = 'block';
            oldCaseOptions.style.display = 'none';
        } else if (value === 'oldcase') {
            newCaseOptions.style.display = 'none';
            oldCaseOptions.style.display = 'block';
        } else {
            newCaseOptions.style.display = 'none';
            oldCaseOptions.style.display = 'none';
        }
    }
    function showLoadingOverlay() {
      $(".loading-overlay").show();
    }

    function hideLoadingOverlay() {
      $(".loading-overlay").hide();
    }
    function loadHTML(url, elementId) {
      fetch(url)
        .then((response) => response.text())
        .then((data) => {
          document.getElementById(elementId).innerHTML = data;
        });
    }

    loadHTML("header.html", "header-placeholder");
    loadHTML("footer.html", "footer-placeholder");
    function checkAvailability() {
      var serviceId = $("#serviceType").val();
      var date = $("#date").val();
      var timeSlot = $("#time_slot").val();

      if (serviceId && date && timeSlot) {
        showLoadingOverlay();
        $.get("checkAvailability", {
          serviceId: serviceId,
          date: date,
          timeSlot: timeSlot,
        })
          .done(function (data) {
            alert(
              data.available ? "Slots available!" : "No slots available."
            );
          })
          .fail(function (jqXHR, textStatus, errorThrown) {
            console.error(
              "Failed to check availability:",
              textStatus,
              errorThrown
            );
          })
          .always(function () {
            hideLoadingOverlay();
          });
      }
    }

    function loadDynamicOptions(action, targetId, parentId, clearTargets) {
      var parentIdValue = parentId ? $(parentId).val() : null;
      showLoadingOverlay();
      $.get("dynamicForm", { action: action, id: parentIdValue })
        .done(function (data) {
          $(targetId).html(data);
          if (clearTargets) {
            clearTargets.forEach(function (target) {
              $(target).html("<option value=''>Select Option</option>");
            });
          }
        })
        .fail(function (jqXHR, textStatus, errorThrown) {
          console.error(
            "Failed to load dynamic options:",
            textStatus,
            errorThrown
          );
        })
        .always(function () {
          hideLoadingOverlay();
        });
    }

    $("#state").change(function () {
      loadDynamicOptions("districts", "#district", "#state", [
        "#courtComplex",
        "#kendra",
      ]);
    });

    $("#district").change(function () {
      loadDynamicOptions("courtComplexes", "#courtComplex", "#district", [
        "#kendra",
      ]);
    });

    $("#courtComplex").change(function () {
      loadDynamicOptions("sewaKendras", "#kendra", "#courtComplex");
    });

    $("#serviceType").change(function () {
      var serviceId = $(this).val();
      if (serviceId) {
        showLoadingOverlay();
        fetch(`getServices?serviceId=${serviceId}`)
          .then((response) => response.text())
          .then((html) => {
            document.getElementById("serviceFormContainer").innerHTML =
              html;
            $("#date, #time_slot").change(function () {
              checkAvailability();
            });

            flatpickr("#date", {
              minDate: "today",
              maxDate: new Date().fp_incr(1),
            });
          })
          .catch((error) =>
            console.error("Error loading service form:", error)
          )
          .finally(() => hideLoadingOverlay());
      } else {
        $("#serviceFormContainer").empty();
      }
    });

    // Show loading overlay on page load
    $(".loading-overlay").show();

    // Function to show loading overlay
    function showLoadingOverlay() {
      $(".loading-overlay").show();
    }

    // Function to hide loading overlay
    function hideLoadingOverlay() {
      $(".loading-overlay").hide();
    }

    // Load states
    $.get("dynamicForm", { action: "states" }, function (data) {
      $("#state").html(data);
    }).always(function () {
      hideLoadingOverlay();
    });

    // On state change, load districts
    $("#state").change(function () {
      var stateId = $(this).val();
      showLoadingOverlay();
      $.get(
        "dynamicForm",
        { action: "districts", id: stateId },
        function (data) {
          $("#district").html(data);
          $("#courtComplex").html(
            "<option value=''>Select Court Complex</option>"
          );
          $("#kendra").html(
            "<option value=''>Select E-Sewa Kendra</option>"
          );
        }
      ).always(function () {
        hideLoadingOverlay();
      });
    });

    // On district change, load court complexes
    $("#district").change(function () {
      var districtId = $(this).val();
      showLoadingOverlay();
      $.get(
        "dynamicForm",
        { action: "courtComplexes", id: districtId },
        function (data) {
          $("#courtComplex").html(data);
          $("#kendra").html(
            "<option value=''>Select E-Sewa Kendra</option>"
          );
        }
      ).always(function () {
        hideLoadingOverlay();
      });
    });

    // On court complex change, load e-Sewa Kendras
    $("#courtComplex").change(function () {
      var courtComplexId = $(this).val();
      showLoadingOverlay();
      $.get(
        "dynamicForm",
        { action: "sewaKendras", id: courtComplexId },
        function (data) {
          $("#kendra").html(data);
        }
      ).always(function () {
        hideLoadingOverlay();
      });
    });

    // Load service types on page load
    $.get("dynamicForm", { action: "serviceTypes" }, function (data) {
      $("#serviceType").html(data);
    }).always(function () {
      hideLoadingOverlay();
    });

    function loadServiceForm(serviceId) {
      $(".loading-overlay").show();
      fetch(`getServices?serviceId=${serviceId}`)
        .then((response) => response.text())
        .then((html) => {
          document.getElementById("serviceFormContainer").innerHTML = html;
          $(".loading-overlay").hide();
        });
      flatpickr("#date", {
        minDate: "today",
        maxDate: new Date().fp_incr(1),
      });
    }
  });
  window.onload = function () {
    var errorMessage = '<%= request.getAttribute("errorMessage") %>';
    if (errorMessage) {
      document.getElementById("error-message").style.display = "block";
      document.getElementById("error-text").innerText = errorMessage;
    }
  };