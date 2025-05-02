# HTML to PDF Conversion Guide

This guide explains how to convert the HTML documentation to PDF format.

## Prerequisites

The conversion script supports multiple methods for converting HTML to PDF:

1. **wkhtmltopdf** (recommended): A command-line tool that renders HTML to PDF
   - Download from: https://wkhtmltopdf.org/downloads.html
   - Install and ensure it's in your system PATH

2. **WeasyPrint** (alternative): A Python library for HTML to PDF conversion
   - Install with: `pip install weasyprint`
   - Note: WeasyPrint has additional dependencies that vary by operating system

3. **Manual browser printing** (fallback): If the above methods fail, the script will open the HTML file in your default browser for manual printing to PDF

## How to Convert

1. Make sure you have the HTML file (`README.html`) in the project directory
2. Run the conversion script:

```bash
python convert_to_pdf.py
```

3. The script will:
   - First try to use wkhtmltopdf
   - If that fails, try WeasyPrint
   - If both fail, open the HTML in your browser for manual printing
   - Generate a PDF file named `README.pdf` in the project directory

## Customizing the Output

If you want to customize the PDF output:

- For wkhtmltopdf: Edit the options in the `convert_html_to_pdf_wkhtmltopdf` function in `convert_to_pdf.py`
- For WeasyPrint: You can modify the WeasyPrint settings in the `try_weasyprint` function
- For browser printing: Use your browser's print dialog to adjust settings before saving as PDF

## Troubleshooting

- If you get an error about missing modules, make sure you have all required Python packages installed
- If the PDF doesn't look right, try a different conversion method
- For image rendering issues, ensure all image paths in the HTML file are correct
- If the script can't find wkhtmltopdf, make sure it's installed and in your system PATH

## Manual Conversion

If you prefer to convert manually:

1. Open the HTML file in a web browser
2. Use the browser's print function (Ctrl+P or Cmd+P)
3. Select "Save as PDF" as the destination
4. Adjust margins, scale, and other settings as needed
5. Save the PDF file