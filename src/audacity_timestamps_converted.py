"""
merge_labels.py

Prompts the user to select between 1 and 7 "Labels x.txt" files
(x = 1..7), reads each one (tab-separated, 3 columns: timestamp,
timestamp, text), and merges them into a single combined array.

Combined array format:
    [timestamp, text_from_Labels_1, text_from_Labels_2, ..., text_from_Labels_7]

- Each row's text columns are placed according to the file's number
  "x" (e.g. text from "Labels 3.txt" always goes in the column for
  file 3), regardless of the order the files were selected in.
- If a file for a given x was not selected, or a given timestamp
  doesn't appear in that file, the corresponding cell is filled
  with 0.
- Rows are matched across files by identical timestamp (the value
  found in columns 1 and 2 of each source file, which are identical
  to each other).

The result is written to "combined_labels.csv" and also printed.
"""

import os
import re
import sys
import csv
import tkinter as tk
from tkinter import filedialog, messagebox



def select_files():
    """Open a file dialog and let the user pick between 1 and 7 text files."""
    root = tk.Tk()
    root.withdraw()  # hide the empty root window

    while True:
        path = filedialog.askopenfilename(
            title="Select 'Labels x.txt' files",
            filetypes=[("Text files", "*.txt"), ("All files", "*.*")],
        )

        if not path:
            # User hit cancel / closed the dialog
            if messagebox.askyesno("No file selected", "No file was selected. Quit?"):
                root.destroy()
                sys.exit("No file selected. Exiting.")
            else:
                continue


        root.destroy()
        return path

        messagebox.showerror(
            "Invalid selection",
            f"Please select between a file "
            f"(you selected {len(paths)}).",
        )




def read_labels_file(path):
    """
    Read a 'Labels x.txt' file (tab-separated, 3 columns: timestamp,
    timestamp, text). Drop the first column, returning a list of
    [timestamp, text] rows.
    """
    rows = []
    with open(path, "r", encoding="utf-8-sig") as f:
        for line_num, raw_line in enumerate(f, start=1):
            line = raw_line.rstrip("\n\r")
            if not line.strip():
                continue  # skip blank lines

            parts = line.split("\t")
            if len(parts) < 3:
                print(
                    f"Warning: skipping malformed line {line_num} in "
                    f"'{os.path.basename(path)}' (expected 3 tab-separated "
                    f"columns, got {len(parts)})."
                )
                continue

            timestamp_str, _second_timestamp_str, text = parts[0], parts[1], parts[2].strip()

            try:
                timestamp = float(timestamp_str)
            except ValueError:
                print(
                    f"Warning: skipping line {line_num} in "
                    f"'{os.path.basename(path)}' (timestamp '{timestamp_str}' "
                    f"is not a valid number)."
                )
                continue

            # Drop the first column -> keep [timestamp, text]
            #rows.append([round(timestamp * 20), text])
            if len(text) > 0:#if there is text
                rows.append([timestamp, text])

    #break up the data by timestamp
    matrices = []
    current_matrix = []
    for row in rows:
        # If current_matrix exists and the timestamp decreases, save and start a new matrix
        if current_matrix and row[0] < current_matrix[-1][0]:
            matrices.append(current_matrix)
            current_matrix = []
        current_matrix.append(row)

    if current_matrix:
        matrices.append(current_matrix)
            
    return matrices

def binary_search_column(matrix, target, col_idx):
    low = 0
    high = len(matrix) - 1  # Total number of rows

    while low <= high:
        mid = (low + high) // 2
        # Keep the column index fixed while checking the middle row
        mid_val = matrix[mid][col_idx]

        if mid_val == target:
            return mid  # Returns the row index where target was found
        elif mid_val < target:
            low = mid + 1
        else:
            high = mid - 1

    return -1  # Target not found

def get_row_of_timestamp(timestamp, combined):
    row = binary_search_column(combined, timestamp, 0)
    return row
    
def merge_files(file_data_by_index):
    combined = file_data_by_index[0]
    #fill zeros
    for row in combined:
        row += ["0"] * 6
    for i in range(1, len(file_data_by_index)):
        current = file_data_by_index[i]
        for j in range(len(current)):
            row = current[j]
            timestamp_index = get_row_of_timestamp(row[0], combined)
            if timestamp_index > -1:#if timestamp is in combined column
                #add row[1] to combined
                combined[timestamp_index][i + 1] = row[1]
                
            else:#if timestamp not in combined
                #create new row and fill with zeros
                new_row = [row[0], "0", "0", "0", "0", "0", "0", "0"]
                new_row[i + 1] = row[1]
                combined.append(new_row)
                combined.sort(key=lambda x: x[0])
                #insert timestamp into combined and fill 0s
    return combined
        


def main():
    path = select_files()

    data = read_labels_file(path)
    #sort by time
    #data.sort(key=lambda x: x[0])
    #convert from seconds to ticks

    
    length = len(data)
    print(f"Loaded {length} labels from '{os.path.basename(path)}' ")
    

    #for textfile in file_data_by_index:  
    #    print("\nstart\n")  
    #    for row in textfile:
    #        print(row)
    
    #print(file_data_by_index)
    combined = []
    #TODO combine data
    #TODO convert to relative
    
    combined = merge_files(data)
    combined.sort(key=lambda x: x[0])
    #Make time relative to previous index:
    '''
    relative = []
    for i in range(len(combined)):
        if i == 0:
            relative.append(0)
        else:
            relative.append(combined[i][0] - combined[i-1][0])

    for i in range(len(combined)):
        combined[i][0] = relative[i]

    '''
    #print delays
    print("double[] times = {", end="")
    for i in range(len(combined)):
        print(combined[i][0], end="")
        if i < len(combined) - 1:
            print(", ", end="")
    print("};")
    print("String[] commands = {", end="")
    #print commands
    for i in range(len(combined)):
        command = "\""
        for j in range(1, len(combined[i])):
            command += combined[i][j]
            if j < len(combined[i]) - 1:
                command += " "
        command += "\""
        print(command, end="")
        if i < len(combined) - 1:
            print(", ", end="")
    print("};")
        
    #print("\nCombined array:")
    #for row in combined:
    #    print(row)



if __name__ == "__main__":
    main()
