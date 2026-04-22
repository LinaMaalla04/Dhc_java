-- Seed large variety of medications commonly found in OpenFDA labels.
-- Compatible with table: medicament(nom_medicament, categorie, dosage, forme, date_expiration, stock)

INSERT INTO medicament (nom_medicament, categorie, dosage, forme, date_expiration, stock) VALUES
('Paracetamol', 'Analgésique / Antipyrétique', '500 mg', 'Comprimé', '2028-12-31', 300),
('Ibuprofen', 'AINS', '400 mg', 'Comprimé', '2028-12-31', 260),
('Aspirin', 'AINS', '100 mg', 'Comprimé', '2028-12-31', 220),
('Diclofenac', 'AINS', '50 mg', 'Comprimé', '2028-12-31', 180),
('Naproxen', 'AINS', '250 mg', 'Comprimé', '2028-12-31', 140),

('Amoxicillin', 'Antibiotique', '500 mg', 'Gélule', '2028-12-31', 240),
('Azithromycin', 'Antibiotique', '500 mg', 'Comprimé', '2028-12-31', 170),
('Ciprofloxacin', 'Antibiotique', '500 mg', 'Comprimé', '2028-12-31', 130),
('Nitrofurantoin', 'Antibiotique urinaire', '100 mg', 'Gélule', '2028-12-31', 110),
('Fosfomycin', 'Antibiotique urinaire', '3 g', 'Sachet', '2028-12-31', 90),
('Metronidazole', 'Antibiotique / Antiparasitaire', '500 mg', 'Comprimé', '2028-12-31', 120),

('Cetirizine', 'Antihistaminique', '10 mg', 'Comprimé', '2028-12-31', 190),
('Loratadine', 'Antihistaminique', '10 mg', 'Comprimé', '2028-12-31', 170),
('Fexofenadine', 'Antihistaminique', '120 mg', 'Comprimé', '2028-12-31', 140),
('Montelukast', 'Anti-asthmatique', '10 mg', 'Comprimé', '2028-12-31', 120),
('Salbutamol', 'Bronchodilatateur', '100 mcg', 'Inhalateur', '2028-12-31', 100),
('Budesonide', 'Corticostéroïde inhalé', '200 mcg', 'Inhalateur', '2028-12-31', 80),

('Omeprazole', 'IPP', '20 mg', 'Gélule', '2028-12-31', 210),
('Pantoprazole', 'IPP', '40 mg', 'Comprimé', '2028-12-31', 150),
('Loperamide', 'Antidiarrhéique', '2 mg', 'Gélule', '2028-12-31', 120),
('Ondansetron', 'Antiemétique', '4 mg', 'Comprimé', '2028-12-31', 100),
('Oral Rehydration Salts', 'Réhydratation', 'SRO', 'Sachet', '2028-12-31', 140),

('Amlodipine', 'Antihypertenseur', '5 mg', 'Comprimé', '2028-12-31', 160),
('Losartan', 'Antihypertenseur', '50 mg', 'Comprimé', '2028-12-31', 130),
('Lisinopril', 'Antihypertenseur', '10 mg', 'Comprimé', '2028-12-31', 110),
('Metformin', 'Antidiabétique', '850 mg', 'Comprimé', '2028-12-31', 180),
('Insulin Glargine', 'Antidiabétique', '100 UI/mL', 'Injection', '2028-12-31', 70),

('Atorvastatin', 'Hypolipémiant', '20 mg', 'Comprimé', '2028-12-31', 120),
('Levothyroxine', 'Thyroïde', '50 mcg', 'Comprimé', '2028-12-31', 100),
('Sumatriptan', 'Antimigraineux', '50 mg', 'Comprimé', '2028-12-31', 90),
('Oseltamivir', 'Antiviral', '75 mg', 'Gélule', '2028-12-31', 80),
('Vitamin C', 'Supplément', '500 mg', 'Comprimé', '2028-12-31', 200);
