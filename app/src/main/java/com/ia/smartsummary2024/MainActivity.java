package com.ia.smartsummary2024;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ia.smartsummary2024.HttpSolicitud;
import com.ia.smartsummary2024.Mensaje;
import com.ia.smartsummary2024.R;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor;
import com.itextpdf.kernel.pdf.canvas.parser.listener.LocationTextExtractionStrategy;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.property.TextAlignment;



import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okhttp3.ResponseBody;



public class MainActivity extends AppCompatActivity {
    private EditText et_enviar, et_RecibirJson;
    private static final int REQUEST_CODE_SELECCIONAR_PDF = 1;
    private Button btn_Enviar, btn_subirArchivo, btn_descargarPDF;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incio);

        et_enviar= findViewById(R.id.et_ingresetexto);
        et_RecibirJson = findViewById(R.id.et_respuesta_resumen);
        btn_Enviar = findViewById(R.id.btn_resumir);
        btn_subirArchivo = findViewById(R.id.btn_cargar_archivo);
        btn_descargarPDF = findViewById(R.id.btn_descargarPDF);


        btn_subirArchivo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_RecibirJson.setText("");
                seleccionarPDF();
            }
        });
        btn_Enviar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                et_RecibirJson.setText("Procesando, Por favor Espere...");

                Mensaje mensaje = new Mensaje(et_enviar.getText().toString());
                HttpSolicitud.post("http://192.168.1.213:5002/send", mensaje, new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        // Error al realizar la llamada HTTP
                        // Manejar el error
                        Log.e("ErrorLlamado", e.toString());
                    }
                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        if (response.isSuccessful()) {
                            // La solicitud se realizó correctamente y el servidor devolvió un código de respuesta 2xx
                            ResponseBody responseBody = response.peekBody(Long.MAX_VALUE);
                            String respuesta = responseBody.string();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    et_RecibirJson.setText(respuesta);
                                }
                            });

                        } else {
                            // El servidor devolvió un código de respuesta diferente a 2xx
                            // Manejar el error
                            Log.e("ErrorServe", "Error en la respuesta del servidor: " + response.code());
                        }
                    }
                });
            }
        });


        btn_descargarPDF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                generarPDF();
            }
        });
    }

    private void generarPDF() {
        // Obtener el texto del EditText
        String contenido = et_RecibirJson.getText().toString();

        // Crear un AlertDialog para ingresar el nombre del archivo PDF
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Guardar PDF");
        builder.setMessage("Ingresa el nombre del archivo:");

        // Crear un EditText dentro del AlertDialog
        final EditText etNombreArchivo = new EditText(this);
        etNombreArchivo.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(etNombreArchivo);

        // Agregar botones de "Aceptar" y "Cancelar" al AlertDialog
        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombreArchivo = etNombreArchivo.getText().toString().trim(); // Obtener el nombre del archivo ingresado por el usuario

                // Crear un archivo PDF en la ubicación deseada
                File archivoPDF = new File(getExternalFilesDir(null), nombreArchivo + ".pdf");

                try {
                    // Inicializar el documento PDF
                    PdfWriter writer = new PdfWriter(new FileOutputStream(archivoPDF));
                    PdfDocument pdf = new PdfDocument(writer);
                    Document document = new Document(pdf, PageSize.A4);

                    // Agregar el contenido del EditText al documento PDF
                    PdfFont font = PdfFontFactory.createFont();
                    Paragraph paragraph = new Paragraph(contenido)
                            .setFont(font)
                            .setFontSize(12)
                            .setTextAlignment(TextAlignment.LEFT);

                    document.add(paragraph);

                    // Cerrar el documento PDF
                    document.close();

                    // Mostrar el diálogo para abrir el PDF
                    abrirPDF(archivoPDF);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        builder.setNegativeButton("Cancelar", null);

        // Mostrar el AlertDialog
        builder.create().show();
    }

    //aca se despliega una opcion para poder seleccionar con que aplicacion queremos abrir el pdf generado
    private void abrirPDF(File archivoPDF) {
        // Obtener la URI del archivo utilizando FileProvider
        Uri archivoUri = FileProvider.getUriForFile(this, "com.example.smartsummary.fileprovider", archivoPDF);

        // Crear un Intent para abrir el archivo PDF
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(archivoUri, "application/pdf");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        // Verificar si hay aplicaciones disponibles para abrir el archivo
        PackageManager packageManager = getPackageManager();
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(this, "No se encontró ninguna aplicación para abrir el PDF", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_SELECCIONAR_PDF && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                cargarPDFDesdeUri(uri);
            }
        }
    }



    private void cargarPDFDesdeUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);

            // Crear un objeto PdfDocument para leer el PDF desde el flujo de entrada
            PdfDocument pdfDocument = new PdfDocument(new PdfReader(inputStream));

            // Crear un objeto StringBuilder para almacenar el contenido del PDF
            StringBuilder contenidoPDF = new StringBuilder();

            // Recorrer las páginas del PDF y extraer su contenido
            int numPages = pdfDocument.getNumberOfPages();
            for (int i = 1; i <= numPages; i++) {
                // Obtener el contenido de la página como una cadena de texto
                String pageContent = PdfTextExtractor.getTextFromPage(pdfDocument.getPage(i), new LocationTextExtractionStrategy());

                // Agregar el contenido de la página al StringBuilder
                contenidoPDF.append(pageContent).append("\n");
            }

            // Mostrar el contenido en el EditText
            et_enviar.setText(contenidoPDF.toString());

            // Cerrar el objeto PdfDocument
            pdfDocument.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void seleccionarPDF() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("application/pdf");
        startActivityForResult(intent, REQUEST_CODE_SELECCIONAR_PDF);
    }
}