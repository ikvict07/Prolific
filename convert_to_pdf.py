import os
import sys
import subprocess
import webbrowser
import platform
import time

def convert_html_to_pdf_wkhtmltopdf(html_file, pdf_file):
    """
    Convert HTML file to PDF using wkhtmltopdf.

    Args:
        html_file (str): Path to the HTML file
        pdf_file (str): Path to the output PDF file

    Returns:
        bool: True if conversion was successful, False otherwise
    """
    try:
        # Check if wkhtmltopdf is installed
        try:
            subprocess.run(['wkhtmltopdf', '--version'], 
                          stdout=subprocess.PIPE, 
                          stderr=subprocess.PIPE, 
                          check=True)
        except (subprocess.SubprocessError, FileNotFoundError):
            print("Error: wkhtmltopdf is not installed or not in PATH.")
            print("Please install wkhtmltopdf from https://wkhtmltopdf.org/downloads.html")
            return False

        # Convert HTML to PDF
        print(f"Converting {html_file} to {pdf_file} using wkhtmltopdf...")

        # Use wkhtmltopdf with options for better rendering
        cmd = [
            'wkhtmltopdf',
            '--enable-local-file-access',  # Allow access to local files
            '--page-size', 'A4',           # Set page size to A4
            '--margin-top', '10mm',        # Set margins
            '--margin-right', '10mm',
            '--margin-bottom', '10mm',
            '--margin-left', '10mm',
            '--print-media-type',          # Use print media type
            '--no-background',             # Don't print background
            html_file,                     # Input HTML file
            pdf_file                       # Output PDF file
        ]

        result = subprocess.run(cmd, 
                               stdout=subprocess.PIPE, 
                               stderr=subprocess.PIPE)

        if result.returncode != 0:
            print(f"Error converting HTML to PDF: {result.stderr.decode()}")
            return False

        print(f"Successfully converted {html_file} to {pdf_file}")
        return True

    except Exception as e:
        print(f"An error occurred: {str(e)}")
        return False

def try_weasyprint(html_file, pdf_file):
    """
    Try to convert HTML to PDF using WeasyPrint.

    Args:
        html_file (str): Path to the HTML file
        pdf_file (str): Path to the output PDF file

    Returns:
        bool: True if conversion was successful, False otherwise
    """
    try:
        try:
            from weasyprint import HTML
            print("Using WeasyPrint for conversion...")
            HTML(html_file).write_pdf(pdf_file)
            print(f"Successfully converted {html_file} to {pdf_file} using WeasyPrint")
            return True
        except ImportError:
            print("WeasyPrint is not installed.")
            print("Please install it with: pip install weasyprint")
            return False
    except Exception as e:
        print(f"Error using WeasyPrint: {str(e)}")
        return False

def open_in_browser(html_file):
    """
    Open the HTML file in the default browser for manual printing to PDF.

    Args:
        html_file (str): Path to the HTML file
    """
    print(f"Opening {html_file} in your default browser.")
    print("Please use the browser's print function (Ctrl+P or Cmd+P) to save as PDF.")

    # Convert to file URL
    file_url = f"file://{os.path.abspath(html_file)}"
    webbrowser.open(file_url)

def convert_html_to_pdf(html_file, pdf_file):
    """
    Convert HTML file to PDF using available methods.

    Args:
        html_file (str): Path to the HTML file
        pdf_file (str): Path to the output PDF file

    Returns:
        bool: True if conversion was successful, False otherwise
    """
    # Try wkhtmltopdf first
    if convert_html_to_pdf_wkhtmltopdf(html_file, pdf_file):
        return True

    print("Trying alternative conversion method...")

    # Try WeasyPrint as a fallback
    if try_weasyprint(html_file, pdf_file):
        return True

    # If all else fails, open in browser for manual printing
    print("Automatic PDF conversion failed. Opening in browser for manual printing...")
    open_in_browser(html_file)

    # Wait for user confirmation
    input("Press Enter after you've saved the PDF manually...")

    # Check if the PDF file exists now
    if os.path.exists(pdf_file):
        print(f"PDF file found at {pdf_file}")
        return True
    else:
        print(f"PDF file not found at {pdf_file}. Please save it manually to this location.")
        return False

def main():
    # Get the directory of the script
    script_dir = os.path.dirname(os.path.abspath(__file__))

    # Set the paths for the HTML and PDF files
    html_file = os.path.join(script_dir, 'README.html')
    pdf_file = os.path.join(script_dir, 'README.pdf')

    # Check if the HTML file exists
    if not os.path.exists(html_file):
        print(f"Error: HTML file {html_file} does not exist.")
        return 1

    # Convert HTML to PDF
    success = convert_html_to_pdf(html_file, pdf_file)

    if success:
        print(f"PDF file created: {pdf_file}")
        return 0
    else:
        print("Failed to create PDF file.")
        return 1

if __name__ == "__main__":
    sys.exit(main())
