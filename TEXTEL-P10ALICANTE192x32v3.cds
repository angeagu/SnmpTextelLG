q-- ***************************************************************************
-- *
-- * Copyright (c) 2019 Textel Marimon s.a. Inc. All Rights Reserved.
-- *
-- * Module Name:  TEXTEL-P10ALICANTE192x32v3
-- *
-- * Description:  DEFINE OBJETOS PARA MANEJAR TELEINDICADORES  
-- * 
-- ***************************************************************************
                                 $"Nombre del fabricante del display."                            "NSmero de serie del fabricante"                       ""identificador del teleindicador."                       "calle y numero."                       0"Estado general del teleindicador (Ok / Error)."                               *"Estado USB tarjeta errores (Ok / Error)."                       "Encendido (0 = OFF / 1 = ON)."                       $"Estado puerta (Abierta / Cerrada)."                       "Temperatura interior (°C)."                       '"Estado termostato (0 = OFF / 1 = ON)."                       '"Estado del ventilador 1 (Ok / Error)."                       '"Estado del ventilador 2 (Ok / Error)."                       )"Nivel fotocelula (1 - 100 / 0 = error)."                       "Numero de leds con error."                       '"Numero de filas de modulos con error."                       "Reset Software (1 = SI)."                       %"Reset Software Sin Apagar (1 = SI)."                       "Reset Hardware (1 = SI)."                       "Test RGB (1 = SI)."                       "Test Audio (1 = SI)."                           "Error Usb."                 "Puerta abierta."                 "Temperatura excedida."                 "Fallo en ventiladores."                 "Leds con error."                 "Modulos Led con error."                 "Impacto (en Gs)."                 "Fotocelula con error."                     +"IP del administrador para envio de traps."                       /"Puerto del administrador para envio de traps."                               :"identificador interno del operador
				 (0 - 9999999999)"                       z"modo en que trabaja el panel (
				 0 = deshabilitado,
				 1 = normal
				 2 = sin estimaciones
				 3 = mantenimiento)"                       3"multiidioma habilitado (
				 0 = no
				 1 = si)"                       @"array de identificadores de idiomas,
				 uno como minimo (ES)"                           +"listado paradas para el ambito del panel."                       '"Endpoint del servicio StopMonitoring."                       *"Endpoint del servicio SituationExchange."                       -"Tiempo entre llamadas SM
				 5 - 600 (seg)"                       -"Tiempo entre llamadas SX
				 5 - 600 (seg)"                       R"Modo de alternancia (
				 0 = alterno M1-M2-X1-X2
				 1 = seguido M1-X1-M2-X2)"                       3"duracin pantalla estimaciones
				 5 - 300 (seg)"                       -"duracin pantalla avisos
				 5 - 300 (seg)"                       9"velocidad texto horizontal (0 - 100)
				 50 = standard"                       7"velocidad texto vertical (0 - 100)
				 50 = standard"                       @"color hexadecimal pantalla estimaciones
				 (000000 - FFFFFF)"                       A"color hexadecimal titulo pantalla avisos
				 (000000 - FFFFFF)"                       @"color hexadecimal texto pantalla avisos
				 (000000 - FFFFFF)"                       6"tramo en que se muestra textoProx
				 5 - 300 (seg)"                       D"array textoProx en diferentes idiomas
				 (cdigo idioma / texto)"                       D"tramo durante el que se muestra estimacion
				 1 - 1440 (minutos)"                       V"periodo sin estimaciones validas
				 para mostrar defaultText
				 1 - 60 (minutos)"                       "texto a mostrar cuando Error"                       &"texto a mostrar cuando Mantenimiento"                       0"aadir fechaHora a defaultText y mantenimiento"                       C"array franjasBrillo
				 (hora HH:MM / porcentaje brillo 0 - 100)"                          