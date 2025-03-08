$('#btn_pagos').click(function(e) {
    e.preventDefault(); 
    
    Swal.fire({
        title: "Pago Recibido",
        text: "¡Ya puedes registrarte!",
        icon: "success",
        confirmButtonText: "Continuar"
    }).then((result) => {
        if (result.isConfirmed) {
           
            $(this).closest('form').submit();
        }
    });
});
