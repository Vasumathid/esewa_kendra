 $(document).ready(function () {
      var isAvailable = false;

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
        var kendraId = $("#kendra").val();
        var courtComplexId = $("#courtComplex").val();

        if (serviceId && date && timeSlot && kendraId && courtComplexId) {
          showLoadingOverlay();
          $.get("checkAvailability", {
            serviceId: serviceId,
            date: date,
            timeSlot: timeSlot,
            kendraId: kendraId,
            courtComplexId: courtComplexId
          })
            .done(function (data) {
              try {
                var response = data;
                if (response.available) {
                  alert("Slots available at the selected E-Sewa Kendra!");
                  isAvailable = true;
                } else {
                  alert("No slots available at the selected E-Sewa Kendra.");
                  if (response.suggestions.length > 0) {
                    var suggestions = response.suggestions.map(function (kendra) {
                      return kendra.name;
                    }).join(', ');
                    alert("Available E-Sewa Kendras: " + suggestions);
                  } else {
                    alert("No other E-Sewa Kendras available for the selected time slot and date.");
                  }
                  isAvailable = false;
                }
              } catch (e) {
                console.error("Failed to parse JSON response:", e);
                alert("An error occurred while checking availability.");
              }
            })
            .fail(function (jqXHR, textStatus, errorThrown) {
              console.error("Failed to check availability:", textStatus, errorThrown);
              alert("An error occurred while checking availability.");
              isAvailable = false;
            })
            .always(function () {
              hideLoadingOverlay();
            });
        } else {
          alert("Please provide all required details.");
        }
      }

      $('#sendOtpButton').click(function () {
        var mobileNumber = $('#phoneNumber').val();
        var phoneRegex = /^[0-9]{10}$/;
        if (!phoneRegex.test(mobileNumber)) {
          $('#message').html('<div class="alert alert-danger">Please Enter Valid Phone number!</div>');
        }
        else {
          $.ajax({
            url: 'sendOtp',
            type: 'POST',
            data: { mobileNumber: mobileNumber },
            success: function (response) {
              if (response.success) {
                $('#otpSection').show();
                $('#message').html('<div class="alert alert-success">OTP sent successfully!</div>');
              } else {
                $('#message').html('<div class="alert alert-danger">Failed to send OTP!</div>');
              }
            }
          });
        }
      });

      $('#verifyOtpButton').click(function () {
        var mobileNumber = $('#phoneNumber').val();
        var otp = $('#otp').val();
        $.ajax({
          url: 'verifyOtp',
          type: 'POST',
          data: { mobileNumber: mobileNumber, otp: otp },
          success: function (response) {
            if (response.success) {
              $('#message').html('<div class="alert alert-success">OTP verified successfully!</div>');
            } else {
              $('#message').html('<div class="alert alert-danger">Invalid OTP!</div>');
            }
          }
        });
      });


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

              flatpickr(".date-Picker", {
                minDate: "today",
                maxDate: "today",
                defaultDate: "today"
              });
              // Check if #case_type element exists and bind the change event
              if ($('#case_type').length != 0) {
                $('div#civil_or_criminal,div#cnr_number').hide();
                $('#case_type').change(function () {
                  handleCaseTypeChange($(this).val());
                });
              }
            })
            .catch((error) =>
              console.error("Error loading service form:", error)
            )
            .finally(() => hideLoadingOverlay());
        } else {
          $("#serviceFormContainer").empty();
        }
      });

      $('input[name="advocateOrParty"]').change(function () {
        if ($('#advocate').is(':checked')) {
          $('#advocateDetails').show();
        } else {
          $('#advocateDetails').hide();
        }
      });

      function handleCaseTypeChange(value) {
        if (value === '1') {
          $('div#civil_or_criminal').show();
          $('div#cnr_number').hide();
        } else if (value === '0') {
          $('div#civil_or_criminal').hide();
          $('div#cnr_number').show();
        } else {
          $('div#civil_or_criminal').hide();
          $('div#cnr_number').hide();
        }
      }


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
      function loadStates() {
        // Load states
        $.get("dynamicForm", { action: "states" }, function (data) {
          $("#state").html(data);
        }).always(function () {
          hideLoadingOverlay();
        });
      }

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
      function loadServiceType() {
        // Load service types on page load
        $.get("dynamicForm", { action: "serviceTypes" }, function (data) {
          $("#serviceType").html(data);
        }).always(function () {
          hideLoadingOverlay();
        });
      }
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

      function validateForm() {
        var isValid = true;
        var errors = [];

        // Clear previous errors
        $('#message').html(''); // Clear previous messages

        // Example validation
        var advocateName = $("#advocateName").val();
        if (advocateName.length < 2 || advocateName.length > 100) {
          isValid = false;
          errors.push("Advocate Name must be between 2 and 100 characters.");
        }

        var phoneNumber = $("#phoneNumber").val();
        var phoneRegex = /^[0-9]{10}$/;
        if (!phoneRegex.test(phoneNumber)) {
          isValid = false;
          errors.push("Phone Number must be 10 digits.");
        }

        var email = $("#email").val();
        var emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (email != '' && !emailRegex.test(email)) {
          isValid = false;
          errors.push("Email Address is not valid.");
        }

        // Validate that dropdowns are not default
        var state = $("#state").val();
        if (state === "") {
          isValid = false;
          errors.push("Please select a State.");
        }

        var district = $("#district").val();
        if (district === "") {
          isValid = false;
          errors.push("Please select a District.");
        }

        var courtComplex = $("#courtComplex").val();
        if (courtComplex === "") {
          isValid = false;
          errors.push("Please select a Court Complex.");
        }

        var kendra = $("#kendra").val();
        if (kendra === "") {
          isValid = false;
          errors.push("Please select an e-Sewa Kendra.");
        }

        var serviceType = $("#serviceType").val();
        if (serviceType === "") {
          isValid = false;
          errors.push("Please select a Service Type.");
        }

        if (!isValid || !isAvailable) {
          $('#message').html('<div class="alert alert-danger">' + errors.join("<br>") + '</div>');
        }

        return isValid;
      }


      $('#registrationForm').on('submit', function (event) {
        $(".loading-overlay").show();
        if (!validateForm() || !isAvailable) {
          event.preventDefault();
          hideLoadingOverlay(); // Hide overlay if the form is invalid
        }
        // }// Prevent the default form submission
        event.preventDefault();

        // Serialize the form data
        var formData = $(this).serialize();

        $.ajax({
          type: 'POST',
          url: 'bookService', // Adjust the URL if needed
          data: formData,
          dataType: 'json',
          success: function (response) {
            $(".loading-overlay").hide();
            if (response.status === 'success') {
              $('#message').html('<div class="alert alert-success">' + response.message + '</div>');
              // $('#tokenNumber').text(response.tokenNumber);
            } else {
              $(".loading-overlay").hide();
              // Handle validation errors or other messages
              $('#message').html('<div class="alert alert-danger">' + response.message + '</div>');
            }
          },
          error: function () {
            $(".loading-overlay").hide();
            // Handle server errors
            $('#message').html('<div class="alert alert-danger">Server error occurred.Please try again later.</div>');
          }
        });
      });
      loadStates();
      loadServiceType();
    
 });

  